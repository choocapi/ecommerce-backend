package com.choocapi.ecommercebackend.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.choocapi.ecommercebackend.enums.CouponType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "coupons")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 50)
    String code;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    CouponType type;

    @Column(nullable = false)
    BigDecimal value;

    Integer usageLimit;
    Integer usedCount;

    Instant startDate;
    Instant endDate;

    Boolean isActive;
}


