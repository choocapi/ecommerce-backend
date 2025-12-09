package com.choocapi.ecommercebackend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class ZalopayConfig {

	@Value("${zalopay.app-id}")
	private String appId;

	@Value("${zalopay.key1}")
	private String key1;

	@Value("${zalopay.key2}")
	private String key2;

	@Value("${zalopay.endpoint}")
	private String endpoint;

	@Value("${zalopay.orderstatus}")
	private String orderstatus;

	@Value("${zalopay.callback-url}")
	private String callbackUrl;

	@Value("${app.frontend-url}")
	private String frontendUrl;
}

