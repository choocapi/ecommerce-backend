package com.choocapi.ecommercebackend.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String sku;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true)
    String slug;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    String imageUrls;

    @Column(nullable = false)
    BigDecimal price;

    BigDecimal originalPrice;
    BigDecimal importPrice;

    @ManyToOne(fetch = FetchType.EAGER)
    Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    Brand brand;

    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    String specifications;

    Integer quantity;
    Integer reservedQuantity;

    Boolean isPublished;
    Instant publishedAt;
    Boolean isFeatured;
}


