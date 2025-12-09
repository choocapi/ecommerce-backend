package com.choocapi.ecommercebackend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

@Configuration
public class GoogleOAuth2Config {

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String clientSecret;

	@Value("${server.servlet.context-path}")
	private String contextPath;

	@Value("${app.base-url}")
	private String baseUrl;

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		// Spring Security OAuth2 Client uses redirect URI pattern:
		// {baseUrl}/login/oauth2/code/{registrationId}
		// Base URL can be configured via app.base-url environment variable
		// Development: http://localhost:8080
		// Production: https://your-domain.com
		String fullBaseUrl = baseUrl + contextPath;
		String redirectUri = fullBaseUrl + "/login/oauth2/code/google";

		ClientRegistration googleRegistration = ClientRegistration.withRegistrationId("google")
				.clientId(clientId)
				.clientSecret(clientSecret)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri(redirectUri)
				.scope("openid", "profile", "email")
				.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
				.tokenUri("https://www.googleapis.com/oauth2/v4/token")
				.userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
				.userNameAttributeName(IdTokenClaimNames.SUB)
				.jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
				.clientName("Google")
				.build();

		return new InMemoryClientRegistrationRepository(googleRegistration);
	}
}
