package com.choocapi.ecommercebackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.CartItem;
import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.entity.User;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
}

