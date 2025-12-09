package com.choocapi.ecommercebackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.entity.UserAddress;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUser(User user);
    Optional<UserAddress> findByIdAndUser(Long id, User user);
    Optional<UserAddress> findByUserAndIsDefaultTrue(User user);
}


