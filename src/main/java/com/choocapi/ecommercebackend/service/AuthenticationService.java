package com.choocapi.ecommercebackend.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.choocapi.ecommercebackend.dto.request.AuthenticationRequest;
import com.choocapi.ecommercebackend.dto.request.IntrospectRequest;
import com.choocapi.ecommercebackend.dto.request.UserCreationRequest;
import com.choocapi.ecommercebackend.dto.response.AuthenticationResponse;
import com.choocapi.ecommercebackend.dto.response.IntrospectResponse;
import com.choocapi.ecommercebackend.dto.response.UserResponse;
import com.choocapi.ecommercebackend.entity.EmailToken;
import com.choocapi.ecommercebackend.entity.RefreshToken;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.enums.EmailTokenType;
import com.choocapi.ecommercebackend.enums.Roles;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.UserMapper;
import com.choocapi.ecommercebackend.repository.EmailTokenRepository;
import com.choocapi.ecommercebackend.repository.RefreshTokenRepository;
import com.choocapi.ecommercebackend.repository.RoleRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    RefreshTokenRepository refreshTokenRepository;
    PasswordEncoder passwordEncoder;
    EmailTokenRepository emailTokenRepository;
    EmailService emailService;

    @NonFinal
    @Value("${jwt.singer-key}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.access-token-duration}")
    protected long ACCESS_TOKEN_DURATION;

    @NonFinal
    @Value("${jwt.refresh-token-duration}")
    protected long REFRESH_TOKEN_DURATION;

    @NonFinal
    @Value("${cors.allowed-origins}")
    protected String FRONTEND_URL;

    @NonFinal
    @Value("${email.token-expiry-minutes.verify}")
    protected long VERIFY_TOKEN_EXP_MINUTES;

    @NonFinal
    @Value("${email.token-expiry-minutes.reset}")
    protected long RESET_TOKEN_EXP_MINUTES;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED);
        }

        boolean isPasswordMatched = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isPasswordMatched) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var accessToken = generateToken(user);

        var refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(REFRESH_TOKEN_DURATION, ChronoUnit.DAYS))
                .user(user)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .authenticated(true)
                .build();
    }

    public UserResponse register(UserCreationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(roleRepository.findByName(Roles.CUSTOMER.name()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST))));
        user = userRepository.save(user);

        // Ensure only one active verify token per user: delete old if exists
        emailTokenRepository.deleteByUserAndType(user, EmailTokenType.VERIFY_EMAIL);

        String token = UUID.randomUUID().toString();
        EmailToken emailToken = EmailToken.builder()
                .token(token)
                .user(user)
                .type(EmailTokenType.VERIFY_EMAIL)
                .expiresAt(Instant.now().plus(VERIFY_TOKEN_EXP_MINUTES, ChronoUnit.MINUTES))
                .build();
        emailTokenRepository.save(emailToken);

        String url = FRONTEND_URL + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(user, url);

        return userMapper.toResponse(user);
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }

        RefreshToken storedRefreshToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        refreshTokenRepository.delete(storedRefreshToken);
    }

    public void verifyEmail(String token) {
        EmailToken t = emailTokenRepository.findByTokenAndType(token, EmailTokenType.VERIFY_EMAIL)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));
        if (t.getExpiresAt().isBefore(Instant.now())) {
            emailTokenRepository.delete(t);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
        User user = t.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        emailTokenRepository.delete(t);
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        emailTokenRepository.deleteByUserAndType(user, EmailTokenType.RESET_PASSWORD);
        String token = UUID.randomUUID().toString();
        EmailToken emailToken = EmailToken.builder()
                .token(token)
                .user(user)
                .type(EmailTokenType.RESET_PASSWORD)
                .expiresAt(Instant.now().plus(RESET_TOKEN_EXP_MINUTES, ChronoUnit.MINUTES))
                .build();
        emailTokenRepository.save(emailToken);
        String url = FRONTEND_URL + "/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(user, url);
    }

    public void resetPassword(String token, String newPassword) {
        EmailToken t = emailTokenRepository.findByTokenAndType(token, EmailTokenType.RESET_PASSWORD)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));
        if (t.getExpiresAt().isBefore(Instant.now())) {
            emailTokenRepository.delete(t);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
        User user = t.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        emailTokenRepository.delete(t);
    }

    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }
        emailTokenRepository.deleteByUserAndType(user, EmailTokenType.VERIFY_EMAIL);
        String token = UUID.randomUUID().toString();
        EmailToken emailToken = EmailToken.builder()
                .token(token)
                .user(user)
                .type(EmailTokenType.VERIFY_EMAIL)
                .expiresAt(Instant.now().plus(VERIFY_TOKEN_EXP_MINUTES, ChronoUnit.MINUTES))
                .build();
        emailTokenRepository.save(emailToken);
        String url = FRONTEND_URL + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(user, url);
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    private SignedJWT verifyToken(String token) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        String userId = signedJWT.getJWTClaimsSet().getSubject();

        boolean verified =
                signedJWT.verify(verifier) && expirationTime.after(new Date()) && userRepository.existsById(userId);

        if (!verified) throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public AuthenticationResponse refreshToken(String refreshToken) throws ParseException, JOSEException {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }

        RefreshToken storedRefreshToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (storedRefreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedRefreshToken);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        } else {
            User user = storedRefreshToken.getUser();
            String newAccessToken = generateToken(user);
            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(storedRefreshToken.getToken())
                    .build();
        }
    }

    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("choocapi.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(ACCESS_TOKEN_DURATION, ChronoUnit.DAYS)
                        .toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error while generating token: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
            });
        }

        return stringJoiner.toString();
    }

    /**
     * Authenticate or register user with Google OAuth2
     * @param email User email from Google
     * @param googleId User ID from Google (sub claim)
     * @param firstName First name from Google
     * @param lastName Last name from Google
     * @param avatarUrl Avatar URL from Google (optional)
     * @return AuthenticationResponse with access token and refresh token
     */
    public AuthenticationResponse authenticateWithGoogle(
            String email, String googleId, String firstName, String lastName, String avatarUrl) {
        // Try to find existing user by email or providerId
        User user = userRepository.findByEmail(email)
                .orElse(userRepository.findByProviderAndProviderId("GOOGLE", googleId)
                        .orElse(null));

        if (user == null) {
            // Create new user
            user = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .avatarUrl(avatarUrl)
                    .provider("GOOGLE")
                    .providerId(googleId)
                    .emailVerified(true) // Google emails are already verified
                    .isActive(true)
                    .roles(Set.of(roleRepository.findByName(Roles.CUSTOMER.name())
                            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST))))
                    .build();
            user = userRepository.save(user);
            log.info("Created new user from Google OAuth: {}", email);
        } else {
            // Update existing user if needed
            boolean updated = false;
            if (user.getProvider() == null || !user.getProvider().equals("GOOGLE")) {
                user.setProvider("GOOGLE");
                user.setProviderId(googleId);
                updated = true;
            }
            if (user.getFirstName() == null && firstName != null) {
                user.setFirstName(firstName);
                updated = true;
            }
            if (user.getLastName() == null && lastName != null) {
                user.setLastName(lastName);
                updated = true;
            }
            if (avatarUrl != null && (user.getAvatarUrl() == null || !user.getAvatarUrl().equals(avatarUrl))) {
                user.setAvatarUrl(avatarUrl);
                updated = true;
            }
            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                user.setEmailVerified(true);
                updated = true;
            }
            if (updated) {
                user = userRepository.save(user);
                log.info("Updated user from Google OAuth: {}", email);
            }
        }

        // Generate tokens
        var accessToken = generateToken(user);

        var refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(REFRESH_TOKEN_DURATION, ChronoUnit.DAYS))
                .user(user)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .authenticated(true)
                .build();
    }
}
