package com.choocapi.ecommercebackend.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.dto.request.CartItemRequest;
import com.choocapi.ecommercebackend.dto.response.CartItemResponse;
import com.choocapi.ecommercebackend.dto.response.CartSummaryResponse;
import com.choocapi.ecommercebackend.entity.CartItem;
import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.CartItemMapper;
import com.choocapi.ecommercebackend.repository.CartItemRepository;
import com.choocapi.ecommercebackend.repository.ProductRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartItemService {
    CartItemRepository repository;
    UserRepository userRepository;
    ProductRepository productRepository;
    CartItemMapper mapper;

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
    }

    public CartItemResponse add(CartItemRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        CartItem cartItem = repository.findByUserAndProduct(user, product)
                .orElse(CartItem.builder().user(user).product(product).quantity(0).build());
        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItem = repository.save(cartItem);
        return mapper.toResponse(cartItem);
    }

    public List<CartItemResponse> batchAdd(List<CartItemRequest> requests) {
        User user = getCurrentUser();
        return requests.stream().map(request -> {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

            CartItem cartItem = repository.findByUserAndProduct(user, product)
                    .orElse(CartItem.builder().user(user).product(product).quantity(0).build());
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem = repository.save(cartItem);
            return mapper.toResponse(cartItem);
        }).toList();
    }

    public List<CartItemResponse> list() {
        User user = getCurrentUser();
        return repository.findByUser(user).stream().map(mapper::toResponse).toList();
    }

    public CartItemResponse update(Long id, Integer quantity) {
        CartItem cartItem = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        cartItem.setQuantity(quantity);
        cartItem = repository.save(cartItem);
        return mapper.toResponse(cartItem);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public CartSummaryResponse getSummary() {
        User user = getCurrentUser();
        List<CartItem> cartItems = repository.findByUser(user);

        int totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        int totalProducts = cartItems.size();

        double totalAmount = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity().doubleValue())
                .sum();

        return CartSummaryResponse.builder()
                .totalItems(totalItems)
                .totalProducts(totalProducts)
                .totalAmount(totalAmount)
                .build();
    }
}

