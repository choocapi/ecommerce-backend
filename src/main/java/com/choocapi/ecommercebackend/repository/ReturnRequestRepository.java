package com.choocapi.ecommercebackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.ReturnRequest;
import com.choocapi.ecommercebackend.entity.User;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long>, JpaSpecificationExecutor<ReturnRequest> {
    List<ReturnRequest> findByUserOrderByCreatedAtDesc(User user);

    boolean existsByOrderId(String orderId);
}
