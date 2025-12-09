package com.choocapi.ecommercebackend.configuration;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.enums.Roles;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.repository.RoleRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

// Seed data
@Slf4j
@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${seed-data.admin-email}")
    String ADMIN_EMAIL;

    @Bean
    public ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {

            for (Roles role : Roles.values()) {
                if (!roleRepository.existsByName(role.name())) {
                    roleRepository.save(role.getRoleEntity());
                    log.info("Role '{}' created.", role.name());
                }
            }

            if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
                String password = "Admin@0556";
                User admin = User.builder()
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(password))
                        .roles(Set.of(roleRepository.findByName(Roles.ADMIN.name()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST))))
                        .emailVerified(true)
                        .build();

                userRepository.save(admin);
                log.warn(
                        "Admin user created with email '{}' and password 'admin'. Please change the password after first login.",
                        ADMIN_EMAIL);
            }
        };
    }
}
