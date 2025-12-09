package com.choocapi.ecommercebackend.controller;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.dto.response.AuthenticationResponse;
import com.choocapi.ecommercebackend.service.AuthenticationService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OAuth2Controller {
	AuthenticationService authenticationService;

	@NonFinal
	@Value("${jwt.refresh-token-duration}")
	protected long REFRESH_TOKEN_DURATION;

	@NonFinal
	@Value("${app.frontend-url}")
	protected String FRONTEND_URL;

	/**
	 * Handle Google OAuth2 callback
	 * This endpoint is called by Google after user authorizes the application
	 */
	@GetMapping("/google/callback")
	public void handleGoogleCallback(
			Authentication authentication,
			HttpServletResponse response) throws IOException {
		try {
			// Get OAuth2User from Authentication
			OAuth2User oauth2User = null;
			if (authentication instanceof OAuth2AuthenticationToken) {
				OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
				oauth2User = oauth2Token.getPrincipal();
			}

			if (oauth2User == null) {
				log.error("OAuth2User is null in callback");
				response.sendRedirect(FRONTEND_URL + "/auth/callback?error=oauth_error");
				return;
			}

			// Extract user information from Google OAuth2 response
			String email = oauth2User.getAttribute("email");
			String googleId = oauth2User.getAttribute("sub");
			String firstName = oauth2User.getAttribute("given_name");
			String lastName = oauth2User.getAttribute("family_name");
			String picture = oauth2User.getAttribute("picture");

			log.info("Google OAuth2 callback received for email: {}", email);

			// Authenticate or register user
			AuthenticationResponse authResponse = authenticationService.authenticateWithGoogle(
					email, googleId, firstName, lastName, picture);

			// Set refresh token as HTTP-only cookie
			ResponseCookie cookie = ResponseCookie.from("refresh_token", authResponse.getRefreshToken())
					.httpOnly(true)
					.secure(true)
					.path("/")
					.maxAge(Duration.ofDays(REFRESH_TOKEN_DURATION))
					.sameSite("Strict")
					.build();
			response.addHeader("Set-Cookie", cookie.toString());

			// Redirect to frontend with access token as query parameter
			// Frontend will extract the token and store it
			String redirectUrl = FRONTEND_URL + "/auth/callback?token=" + authResponse.getAccessToken();
			response.sendRedirect(redirectUrl);

		} catch (Exception e) {
			log.error("Error processing Google OAuth2 callback", e);
			// Redirect to frontend with error
			String errorUrl = FRONTEND_URL + "/auth/callback?error=oauth_error";
			response.sendRedirect(errorUrl);
		}
	}
}
