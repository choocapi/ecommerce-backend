package com.choocapi.ecommercebackend.configuration;

import java.text.ParseException;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.choocapi.ecommercebackend.dto.request.IntrospectRequest;
import com.choocapi.ecommercebackend.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    private final AuthenticationService authenticationService;

    @Autowired
    public CustomJwtDecoder(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Value("${jwt.singer-key}")
    private String SIGNER_KEY;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            var response = authenticationService.introspect(
                    IntrospectRequest.builder().token(token).build());

            if (!response.isValid()) throw new JwtException("Invalid token");
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
