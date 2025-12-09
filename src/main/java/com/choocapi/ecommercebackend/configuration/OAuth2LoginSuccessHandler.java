package com.choocapi.ecommercebackend.configuration;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.choocapi.ecommercebackend.dto.response.AuthenticationResponse;
import com.choocapi.ecommercebackend.service.AuthenticationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthenticationService authenticationService;

	@Value("${jwt.refresh-token-duration}")
	private long REFRESH_TOKEN_DURATION;

	@Value("${app.frontend-url}")
	private String FRONTEND_URL;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		try {
			// Get OAuth2User from Authentication
			OAuth2User oauth2User = null;
			if (authentication instanceof OAuth2AuthenticationToken) {
				OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
				oauth2User = oauth2Token.getPrincipal();
			}

			if (oauth2User == null) {
				log.error("OAuth2User is null in success handler");
				response.sendRedirect(FRONTEND_URL + "/auth/callback?error=oauth_error");
				return;
			}

			// Extract user information from Google OAuth2 response
			String email = oauth2User.getAttribute("email");
			String googleId = oauth2User.getAttribute("sub");
			String firstName = oauth2User.getAttribute("given_name");
			String lastName = oauth2User.getAttribute("family_name");
			String picture = oauth2User.getAttribute("picture");

			log.info("Google OAuth2 success handler - email: {}", email);

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
			String redirectUrl = FRONTEND_URL + "/auth/callback?token=" + authResponse.getAccessToken();
			response.sendRedirect(redirectUrl);

		} catch (Exception e) {
			log.error("Error processing Google OAuth2 success", e);
			response.sendRedirect(FRONTEND_URL + "/auth/callback?error=oauth_error");
		}
	}
}
