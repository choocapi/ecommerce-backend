package com.choocapi.ecommercebackend.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @Column(length = 100)
    String id;

    @Column(nullable = false, unique = true, updatable = false)
    String email;

    @Column(unique = true)
    String phoneNumber;
    String password;
    String firstName;
    String lastName;
    
    // OAuth2 provider information
    String provider; // e.g., "GOOGLE", "FACEBOOK"
    String providerId; // User ID from OAuth2 provider
    LocalDate dateOfBirth;

    @Column(columnDefinition = "TEXT")
    String address;
    @Column(length = 500)
    String avatarUrl;
    Boolean isActive;
    Boolean emailVerified;

    @CreatedDate
    Instant createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Role> roles;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.isActive == null) this.isActive = true;
        if (this.emailVerified == null) this.emailVerified = false;
    }
}
