package com.choocapi.ecommercebackend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class MomoConfig {

	@Value("${momo.partner-code}")
	private String partnerCode;

	@Value("${momo.access-key}")
	private String accessKey;

	@Value("${momo.secret-key}")
	private String secretKey;

	@Value("${momo.redirect-url}")
	private String redirectUrl;

	@Value("${momo.ipn-url}")
	private String ipnUrl;

	@Value("${momo.api-url}")
	private String apiUrl;

	@Value("${momo.request-type}")
	private String requestType;

	@Value("${app.frontend-url}")
	private String frontendUrl;
}

