package com.choocapi.ecommercebackend.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.choocapi.ecommercebackend.dto.request.ReturnRequestRequest;
import com.choocapi.ecommercebackend.dto.response.ReturnRequestResponse;
import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.entity.OrderItem;
import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.entity.ReturnRequest;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.enums.OrderStatus;
import com.choocapi.ecommercebackend.enums.ReturnStatus;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.ReturnRequestMapper;
import com.choocapi.ecommercebackend.repository.OrderItemRepository;
import com.choocapi.ecommercebackend.repository.OrderRepository;
import com.choocapi.ecommercebackend.repository.ProductRepository;
import com.choocapi.ecommercebackend.repository.ReturnRequestRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReturnRequestService {
    ReturnRequestRepository repository;
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    ReturnRequestMapper mapper;

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
    }

    /**
     * Create a new return request
     */
    @Transactional
    public ReturnRequestResponse createReturnRequest(ReturnRequestRequest request) {
        User currentUser = getCurrentUser();

        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Validate order belongs to current user
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Validate order is delivered
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.ORDER_NOT_DELIVERED);
        }

        // Check if return request already exists
        if (repository.existsByOrderId(request.getOrderId())) {
            throw new AppException(ErrorCode.RETURN_REQUEST_ALREADY_EXISTS);
        }

        // Create return request
        ReturnRequest returnRequest = mapper.toEntity(request);
        returnRequest.setOrder(order);
        returnRequest.setUser(currentUser);
        returnRequest.setStatus(ReturnStatus.PENDING);

        returnRequest = repository.save(returnRequest);
        log.info("Created return request {} for order {}", returnRequest.getId(), order.getId());

        return mapper.toResponse(returnRequest);
    }

    /**
     * Search + filter return requests (admin)
     */
    public Page<ReturnRequestResponse> getReturnRequests(int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<ReturnRequest> specification = buildSpecification(search, status);
        return repository.findAll(specification, pageable)
                .map(mapper::toResponse);
    }

    /**
     * Get return request by ID
     */
    public ReturnRequestResponse getReturnRequestById(Long id) {
        ReturnRequest returnRequest = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RETURN_REQUEST_NOT_FOUND));
        return mapper.toResponse(returnRequest);
    }

    /**
     * Get current user's return requests
     */
    public List<ReturnRequestResponse> getMyReturnRequests() {
        User currentUser = getCurrentUser();
        
        return repository.findByUserOrderByCreatedAtDesc(currentUser).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update return request status (admin)
     */
    @Transactional
    public ReturnRequestResponse updateReturnRequest(Long id, ReturnRequestRequest request) {
        ReturnRequest returnRequest = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RETURN_REQUEST_NOT_FOUND));

        ReturnStatus newStatus = request.getStatus();
        if (newStatus == null) {
            throw new AppException(ErrorCode.INVALID_RETURN_STATUS);
        }

        // Validate status transition
        if (returnRequest.getStatus() != ReturnStatus.PENDING && newStatus == ReturnStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_RETURN_STATUS);
        }

        // Update mutable fields
        mapper.update(returnRequest, request);
        returnRequest.setStatus(newStatus);

        Instant now = Instant.now();
        switch (newStatus) {
            case APPROVED:
                returnRequest.setApprovedAt(now);
                break;
            case REJECTED:
                returnRequest.setRejectedAt(now);
                break;
            case COMPLETED:
                returnRequest.setCompletedAt(now);
                // Update order status to RETURNED
                Order order = returnRequest.getOrder();
                order.setStatus(OrderStatus.RETURNED);
                orderRepository.save(order);
                
                // Restore product quantities (return products to inventory)
                for (OrderItem item : orderItemRepository.findByOrder(order)) {
                    Product product = productRepository.findById(item.getProduct().getId())
                            .orElse(null);
                    if (product != null) {
                        Integer currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
                        product.setQuantity(currentQuantity + item.getQuantity());
                        productRepository.save(product);
                        log.info("Restored {} units of product {} to inventory", item.getQuantity(), product.getId());
                    }
                }
                break;
            default:
                break;
        }

        returnRequest = repository.save(returnRequest);
        log.info("Updated return request {} to status {}", id, newStatus);

        return mapper.toResponse(returnRequest);
    }

    private Specification<ReturnRequest> buildSpecification(String search, String status) {
        Specification<ReturnRequest> specification = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(search)) {
            String keyword = "%" + search.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> {
                Join<ReturnRequest, Order> orderJoin = root.join("order", JoinType.LEFT);
                Join<ReturnRequest, User> userJoin = root.join("user", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(orderJoin.get("id")), keyword),
                        cb.like(cb.lower(userJoin.get("email")), keyword),
                        cb.like(cb.lower(root.get("reason")), keyword));
            });
        }

        if (StringUtils.hasText(status)) {
            try {
                ReturnStatus requestedStatus = ReturnStatus.valueOf(status.trim().toUpperCase());
                specification = specification.and(
                        (root, query, cb) -> cb.equal(root.get("status"), requestedStatus));
            } catch (IllegalArgumentException ignored) {
                // ignore invalid status
            }
        }

        return specification;
    }
}
