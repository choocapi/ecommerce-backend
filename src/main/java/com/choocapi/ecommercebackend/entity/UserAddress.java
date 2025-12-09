package com.choocapi.ecommercebackend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_addresses")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    String recipientName;
    String phoneNumber;
    @Column(columnDefinition = "TEXT")
    String addressLine;
    String ward;
    String district;
    String city;
    Boolean isDefault;

    @PrePersist
    void onCreate() {
        if (isDefault == null) isDefault = false;
    }
}


