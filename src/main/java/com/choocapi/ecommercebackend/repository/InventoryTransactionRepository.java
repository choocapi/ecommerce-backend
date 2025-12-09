package com.choocapi.ecommercebackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.InventoryTransaction;
import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.enums.InventoryTransactionType;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Page<InventoryTransaction> findByProduct(Product product, Pageable pageable);
    Page<InventoryTransaction> findByProductAndType(Product product, InventoryTransactionType type, Pageable pageable);
}

