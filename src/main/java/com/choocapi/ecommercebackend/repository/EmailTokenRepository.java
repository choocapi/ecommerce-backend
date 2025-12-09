package com.choocapi.ecommercebackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.EmailToken;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.enums.EmailTokenType;

@Repository
public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {
    Optional<EmailToken> findByTokenAndType(String token, EmailTokenType type);
    Optional<EmailToken> findByUserAndType(User user, EmailTokenType type);
    void deleteByUserAndType(User user, EmailTokenType type);
}


