package com.choocapi.ecommercebackend.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_AUTH = {
            "/auth/**",
            "/oauth2/**",
            "/users/me",
            "/vnpay/**",
            "/momo/**",
            "/zalopay/**",
            "/return-requests/**",
            "/chat",
    };

    private static final String[] PUBLIC_GET_URLS = {
            "/products/**",
            "/categories/**",
            "/brands/**",
            "/product-images/product/**",
            "/product-reviews/product/**",
            "/product-reviews",
            "/articles/**",
            "/banners/**",
            "/coupons/**"
    };

    private final CustomJwtDecoder customJwtDecoder;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.frontend-url}")
    private String FRONTEND_URL;

    @Autowired
    public SecurityConfig(CustomJwtDecoder customJwtDecoder, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.customJwtDecoder = customJwtDecoder;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    /**
     * Use OAuth2 Resource Server for implement JWT authentication
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // OAuth2 login requires session, so we need to allow sessions for OAuth2 flow
                // But we keep STATELESS for JWT-based API calls
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(request -> request
                        // Public endpoints
                        .requestMatchers(PUBLIC_AUTH).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()

                        .anyRequest().authenticated()
                )
                // OAuth2 Login configuration for Google
                // Spring Security automatically handles /login/oauth2/code/{registrationId}
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureUrl(FRONTEND_URL + "/auth/callback?error=oauth_error"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        return httpSecurity.build();
    }

    /**
     * CORS Configuration
     * Allows frontend to make requests from configured origin
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }

    /**
     * Default prefix authorities is SCOPE_
     * hasRole() only match with ROLE_ prefix, so SCOPE_ won't work
     * Role (ADMIN, USER) need add ROLE_ prefix in the token and use hasRole()
     * Permission (CREATE_POST,...) don't need prefix and use hasAuthority()
     * So we set authority prefix to empty here
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
