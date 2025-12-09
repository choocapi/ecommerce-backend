package com.choocapi.ecommercebackend.controller;

import java.text.ParseException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.dto.request.AuthenticationRequest;
import com.choocapi.ecommercebackend.dto.request.ForgotPasswordRequest;
import com.choocapi.ecommercebackend.dto.request.IntrospectRequest;
import com.choocapi.ecommercebackend.dto.request.ResetPasswordRequest;
import com.choocapi.ecommercebackend.dto.request.UserCreationRequest;
import com.choocapi.ecommercebackend.dto.request.VerifyEmailRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.AuthenticationResponse;
import com.choocapi.ecommercebackend.dto.response.IntrospectResponse;
import com.choocapi.ecommercebackend.dto.response.UserResponse;
import com.choocapi.ecommercebackend.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService service;

    @NonFinal
    @Value("${jwt.refresh-token-duration}")
    protected long REFRESH_TOKEN_DURATION;

    // Only for customer registration
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody @Valid UserCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @RequestBody @Valid AuthenticationRequest request, HttpServletResponse response) {

        var result = service.authenticate(request);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", result.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(REFRESH_TOKEN_DURATION))
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok()
                .body(ApiResponse.success(AuthenticationResponse.builder()
                                .accessToken(result.getAccessToken())
                                .authenticated(result.isAuthenticated())
                                .build()));
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody @Valid IntrospectRequest request)
            throws ParseException, JOSEException {
        return ResponseEntity.ok()
                .body(ApiResponse.success(service.introspect(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = "refresh_token") String refreshToken,
            HttpServletResponse response) {

        service.logout(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok()
                .body(ApiResponse.success(null, "Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @CookieValue(value = "refresh_token") String refreshToken, HttpServletResponse response)
            throws ParseException, JOSEException {
        AuthenticationResponse result = service.refreshToken(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", result.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(REFRESH_TOKEN_DURATION))
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok()
                .body(ApiResponse.success(AuthenticationResponse.builder()
                                .accessToken(result.getAccessToken())
                                .authenticated(result.isAuthenticated())
                                .build()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        service.verifyEmail(request.getToken());
        return ResponseEntity.ok().body(ApiResponse.success(null, "Email verified"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        // Always return 200 to avoid email enumeration
        try {
            service.requestPasswordReset(request.getEmail());
        } catch (Exception ignored) {}
        return ResponseEntity.ok().body(ApiResponse.success(null, "If the email exists, an email has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        service.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().body(ApiResponse.success(null, "Password updated"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            service.resendVerification(request.getEmail());
        } catch (Exception ignored) {}
        return ResponseEntity.ok().body(ApiResponse.success(null, "If the email exists, a verification email has been sent"));
    }
}
