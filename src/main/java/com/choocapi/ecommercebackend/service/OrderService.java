package com.choocapi.ecommercebackend.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.choocapi.ecommercebackend.dto.request.OrderItemRequest;
import com.choocapi.ecommercebackend.dto.request.OrderRequest;
import com.choocapi.ecommercebackend.dto.response.CartItemResponse;
import com.choocapi.ecommercebackend.dto.response.OrderItemResponse;
import com.choocapi.ecommercebackend.dto.response.OrderResponse;
import com.choocapi.ecommercebackend.entity.Coupon;
import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.entity.OrderItem;
import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.enums.CouponType;
import com.choocapi.ecommercebackend.enums.OrderStatus;
import com.choocapi.ecommercebackend.enums.PaymentMethod;
import com.choocapi.ecommercebackend.enums.PaymentStatus;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.ProductMapper;
import com.choocapi.ecommercebackend.mapper.UserMapper;
import com.choocapi.ecommercebackend.repository.CouponRepository;
import com.choocapi.ecommercebackend.repository.OrderItemRepository;
import com.choocapi.ecommercebackend.repository.OrderRepository;
import com.choocapi.ecommercebackend.repository.ProductRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository repository;
    OrderItemRepository itemRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    CouponRepository couponRepository;
    CartItemService cartItemService;
    UserMapper userMapper;
    ProductMapper productMapper;

    public OrderResponse create(OrderRequest request) {
        Order order = Order.builder()
                .status(request.getStatus() != null ? OrderStatus.valueOf(request.getStatus().toUpperCase()) : OrderStatus.PENDING)
                .shippingName(request.getShippingName())
                .shippingPhone(request.getShippingPhone())
                .shippingAddress(request.getShippingAddress())
                .shippingWard(request.getShippingWard())
                .shippingDistrict(request.getShippingDistrict())
                .shippingCity(request.getShippingCity())
                .paymentMethod(request.getPaymentMethod() != null ? PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()) : null)
                .paymentStatus(request.getPaymentStatus() != null ? PaymentStatus.valueOf(request.getPaymentStatus().toUpperCase()) : PaymentStatus.PENDING)
                .subtotal(request.getSubtotal())
                .discountAmount(request.getDiscountAmount())
                .totalAmount(request.getTotalAmount())
                .couponCode(request.getCouponCode())
                .build();

        if (request.getCustomerId() != null) {
            User user = userRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            order.setUser(user);
        }

        order = repository.save(order);

        if (request.getItems() != null) {
            for (OrderItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                
                // Check available quantity (quantity - reservedQuantity)
                Integer currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
                Integer currentReservedQuantity = product.getReservedQuantity() != null ? product.getReservedQuantity() : 0;
                Integer availableQuantity = currentQuantity - currentReservedQuantity;
                
                if (availableQuantity < itemReq.getQuantity()) {
                    throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                }
                
                OrderItem item = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .totalPrice(itemReq.getTotalPrice())
                        .build();
                itemRepository.save(item);

                // Update product: reserve quantity (increase reservedQuantity, don't decrease quantity yet)
                product.setReservedQuantity(currentReservedQuantity + itemReq.getQuantity());
                productRepository.save(product);
            }
        }

        return toResponse(order);
    }

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
    }

    public OrderResponse createFromCart(OrderRequest request) {
        // Get current user
        User currentUser = getCurrentUser();
        
        // Get current user's cart items
        List<CartItemResponse> cartItemsResponse = cartItemService.list();
        
        if (cartItemsResponse.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Calculate totals from cart
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemResponse cartItem : cartItemsResponse) {
            BigDecimal itemTotal = cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }

        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        // Create order
        Order order = Order.builder()
                .user(currentUser)
                .status(OrderStatus.PENDING)
                .shippingName(request.getShippingName())
                .shippingPhone(request.getShippingPhone())
                .shippingAddress(request.getShippingAddress())
                .shippingWard(request.getShippingWard())
                .shippingDistrict(request.getShippingDistrict())
                .shippingCity(request.getShippingCity())
                .paymentMethod(request.getPaymentMethod() != null ? PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()) : PaymentMethod.COD)
                .paymentStatus(PaymentStatus.PENDING)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .couponCode(request.getCouponCode())
                .orderedAt(Instant.now())
                .build();

        order = repository.save(order);

        // Create order items from cart items
        for (CartItemResponse cartItemResponse : cartItemsResponse) {
            Product product = productRepository.findById(cartItemResponse.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            
            // Check available quantity (quantity - reservedQuantity)
            Integer currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
            Integer currentReservedQuantity = product.getReservedQuantity() != null ? product.getReservedQuantity() : 0;
            Integer availableQuantity = currentQuantity - currentReservedQuantity;
            
            if (availableQuantity < cartItemResponse.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }

            BigDecimal unitPrice = cartItemResponse.getProduct().getPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(cartItemResponse.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItemResponse.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();
            itemRepository.save(item);

            // Reserve quantity (increase reservedQuantity, don't decrease quantity yet)
            product.setReservedQuantity(currentReservedQuantity + cartItemResponse.getQuantity());
            productRepository.save(product);

            // Delete cart item
            cartItemService.delete(cartItemResponse.getId().longValue());
        }

        return toResponse(order);
    }

    public Page<OrderResponse> list(Pageable pageable, String search, String status, String paymentMethod) {
        Specification<Order> specification = buildSpecification(search, status, paymentMethod);
        return repository.findAll(specification, pageable).map(this::toResponse);
    }

    public OrderResponse get(String id) {
        return toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public List<OrderResponse> getMyOrders() {
        User currentUser = getCurrentUser();
        return repository.findByUserOrderByOrderedAtDesc(currentUser).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getUser() != null ? order.getUser().getId() : null)
                .user(order.getUser() != null ? userMapper.toResponse(order.getUser()) : null)
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingWard(order.getShippingWard())
                .shippingDistrict(order.getShippingDistrict())
                .shippingCity(order.getShippingCity())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .orderedAt(order.getOrderedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .items(itemRepository.findByOrder(order).stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .orderId(item.getOrder().getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .product(item.getProduct() != null ? productMapper.toResponse(item.getProduct()) : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    @Transactional
    public void delete(String id) {
        Order order = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        // Xóa order_items trước khi xóa order để tránh foreign key constraint violation
        List<OrderItem> items = itemRepository.findByOrder(order);
        if (!items.isEmpty()) {
            // Xóa từng item để đảm bảo transaction được xử lý đúng
            for (OrderItem item : items) {
                itemRepository.delete(item);
            }
            itemRepository.flush(); // Flush để đảm bảo các delete được thực thi ngay
        }
        
        // Sau đó mới xóa order
        repository.delete(order);
    }

    public OrderResponse updateOrder(String id, OrderRequest request) {
        Order order = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        // Only allow updating shipping information and status
        // Prevent updating if order is already shipped, delivered, or cancelled
        if (order.getStatus() == OrderStatus.SHIPPED || 
            order.getStatus() == OrderStatus.DELIVERED || 
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        
        // Update shipping information
        if (request.getShippingName() != null) {
            order.setShippingName(request.getShippingName());
        }
        if (request.getShippingPhone() != null) {
            order.setShippingPhone(request.getShippingPhone());
        }
        if (request.getShippingAddress() != null) {
            order.setShippingAddress(request.getShippingAddress());
        }
        if (request.getShippingWard() != null) {
            order.setShippingWard(request.getShippingWard());
        }
        if (request.getShippingDistrict() != null) {
            order.setShippingDistrict(request.getShippingDistrict());
        }
        if (request.getShippingCity() != null) {
            order.setShippingCity(request.getShippingCity());
        }
        
        // Update status if provided
        if (request.getStatus() != null) {
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            // Only allow status updates through specific endpoints (confirm, ship, cancel)
            // For general update, only allow status changes that don't require special handling
            if (newStatus != OrderStatus.PENDING && 
                newStatus != OrderStatus.PROCESSING) {
                throw new AppException(ErrorCode.INVALID_KEY);
            }
            order.setStatus(newStatus);
        }
        
        order = repository.save(order);
        return toResponse(order);
    }

    // Additional methods for order status management with inventory updates
    
    public OrderResponse confirmOrder(String id) {
        Order order = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        order.setStatus(OrderStatus.PROCESSING);
        order.setConfirmedAt(Instant.now());
        order = repository.save(order);
        return toResponse(order);
    }

    public OrderResponse shipOrder(String id) {
        Order order = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(Instant.now());
        order = repository.save(order);

        // Update inventory: release reserved quantity and actually reduce quantity
        for (OrderItem item : itemRepository.findByOrder(order)) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            
            // Release reserved quantity
            Integer currentReservedQuantity = product.getReservedQuantity() != null ? product.getReservedQuantity() : 0;
            product.setReservedQuantity(Math.max(0, currentReservedQuantity - item.getQuantity()));
            
            // Actually reduce quantity (ship the product)
            Integer currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
            product.setQuantity(Math.max(0, currentQuantity - item.getQuantity()));
            
            productRepository.save(product);
        }
        
        return toResponse(order);
    }

    public OrderResponse confirmDelivery(String id) {
        Order order = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        User currentUser = getCurrentUser();
        if (order.getUser() == null || !order.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(Instant.now());
        order = repository.save(order);
        return toResponse(order);
    }

    public OrderResponse cancelOrder(String id) {
        Order order = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order = repository.save(order);

        // Release reserved inventory (only decrease reservedQuantity, don't change quantity)
        for (OrderItem item : itemRepository.findByOrder(order)) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElse(null);
            if (product != null) {
                Integer currentReservedQuantity = product.getReservedQuantity() != null ? product.getReservedQuantity() : 0;
                product.setReservedQuantity(Math.max(0, currentReservedQuantity - item.getQuantity()));
                productRepository.save(product);
            }
        }
        
        return toResponse(order);
    }

    public OrderResponse applyCoupon(String id, String code) {
        Order order = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        Coupon coupon = couponRepository.findByCode(code).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Validate coupon
        Instant now = Instant.now();
        if (coupon.getIsActive() != null && !coupon.getIsActive()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Determine eligible amount (subtotal)
        BigDecimal eligibleAmount = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;

        // Compute discount
        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getType() == CouponType.PERCENTAGE) {
            BigDecimal percent = coupon.getValue();
            discount = eligibleAmount.multiply(percent).divide(new BigDecimal("100"));
        } else if (coupon.getType() == CouponType.FIXED) {
            discount = coupon.getValue();
            if (discount.compareTo(eligibleAmount) > 0) {
                discount = eligibleAmount;
            }
        }

        // Update order totals
        order.setCouponCode(coupon.getCode());
        order.setDiscountAmount(discount);
        BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
        order.setTotalAmount(total);

        order = repository.save(order);

        // Increase coupon usage count upon successful apply
        Integer usedCount = coupon.getUsedCount() == null ? 0 : coupon.getUsedCount();
        coupon.setUsedCount(usedCount + 1);
        couponRepository.save(coupon);

        return toResponse(order);
    }

    private Specification<Order> buildSpecification(String search, String status, String paymentMethod) {
        Specification<Order> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (StringUtils.hasText(search)) {
            String keyword = "%" + search.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("id")), keyword),
                    cb.like(cb.lower(root.get("shippingName")), keyword),
                    cb.like(cb.lower(root.get("shippingPhone")), keyword)
            ));
        }

        if (StringUtils.hasText(status)) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
                specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), orderStatus));
            } catch (IllegalArgumentException ignored) {
                // ignore invalid status filter
            }
        }

        if (StringUtils.hasText(paymentMethod)) {
            try {
                PaymentMethod method = PaymentMethod.valueOf(paymentMethod.trim().toUpperCase());
                specification = specification.and((root, query, cb) -> cb.equal(root.get("paymentMethod"), method));
            } catch (IllegalArgumentException ignored) {
                // ignore invalid payment method filter
            }
        }

        return specification;
    }
}

