package com.choocapi.ecommercebackend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class VNPayConfig {
    
    @Value("${vnpay.pay-url}")
    private String vnpPayUrl;
    
    @Value("${vnpay.api-url}")
    private String vnpApiUrl;
    
    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;
    
    @Value("${vnpay.secret-key}")
    private String vnpSecretKey;
    
    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
}

