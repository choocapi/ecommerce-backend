package com.choocapi.ecommercebackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long>, JpaSpecificationExecutor<Brand> {
    boolean existsBySlug(String slug);
    Optional<Brand> findBySlug(String slug);
}


