package com.choocapi.ecommercebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.Banner;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long>, JpaSpecificationExecutor<Banner> {
}


