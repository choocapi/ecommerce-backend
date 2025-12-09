package com.choocapi.ecommercebackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {
    boolean existsByCode(String code);
    Optional<Coupon> findByCode(String code);
}


