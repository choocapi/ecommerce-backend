package com.choocapi.ecommercebackend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.choocapi.ecommercebackend.enums.OrderStatus;
import com.choocapi.ecommercebackend.enums.PaymentMethod;
import com.choocapi.ecommercebackend.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @Column(length = 100)
    String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    OrderStatus status;

    String shippingName;
    @Column(length = 20)
    String shippingPhone;

    String shippingAddress;

    String shippingWard;
    String shippingDistrict;
    String shippingCity;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    PaymentStatus paymentStatus;

    @Column(name = "vnp_txn_ref")
    String vnPayOrderId;

    @Column(name = "momo_order_id")
    String momoOrderId;

    @Column(name = "zalopay_order_id")
    String zalopayOrderId;

    BigDecimal subtotal;
    @Column(length = 50)
    String couponCode;
    BigDecimal discountAmount;
    @Column(nullable = false)
    BigDecimal totalAmount;

    Instant orderedAt;
    Instant confirmedAt;
    Instant shippedAt;
    Instant deliveredAt;
    Instant cancelledAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.orderedAt == null) {
            this.orderedAt = Instant.now();
        }
    }
}

