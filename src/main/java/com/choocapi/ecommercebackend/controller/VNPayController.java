package com.choocapi.ecommercebackend.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.configuration.VNPayConfig;
import com.choocapi.ecommercebackend.dto.request.VNPayPaymentRequest;
import com.choocapi.ecommercebackend.dto.response.VNPayPaymentResponse;
import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.repository.OrderRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;
import com.choocapi.ecommercebackend.service.VNPayService;
import com.choocapi.ecommercebackend.utils.PaymentHtmlUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/vnpay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VNPayController {

    VNPayService vnPayService;
    OrderRepository orderRepository;
    UserRepository userRepository;
    VNPayConfig vnPayConfig;

    @PostMapping("/create-payment")
    public VNPayPaymentResponse createPayment(
            @RequestBody VNPayPaymentRequest request,
            HttpServletRequest httpRequest) {
        
        // Get current user
        String userId = SecurityContextHolder
                .getContext().getAuthentication().getName();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        
        // Validate order belongs to user
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return vnPayService.createPaymentUrl(request, httpRequest);
    }

    @GetMapping("/return")
    public String paymentReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        log.info("VNPay return with params: {}", params);
        
        // Process payment result and update order status
        Map<String, String> result = vnPayService.handlePaymentReturn(params);
        
        String status = result.get("status");
        String orderId = result.get("orderId");
        
        // Build redirect URL
        String redirectUrl;
        if ("success".equals(status)) {
            redirectUrl = vnPayConfig.getFrontendUrl() + "/checkout/result?status=success&orderId=" + orderId;
        } else if ("failed".equals(status)) {
            redirectUrl = vnPayConfig.getFrontendUrl() + "/checkout/result?status=failed&orderId=" + orderId;
        } else {
            redirectUrl = vnPayConfig.getFrontendUrl() + "/checkout/result?status=error";
        }
        
        log.info("Redirecting to: {}", redirectUrl);
        
        // Return HTML with meta refresh and JavaScript redirect
        response.setContentType("text/html; charset=UTF-8");
        return PaymentHtmlUtil.generateRedirectHtml(redirectUrl);
    }

}

