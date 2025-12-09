package com.choocapi.ecommercebackend.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.choocapi.ecommercebackend.configuration.VNPayConfig;
import com.choocapi.ecommercebackend.dto.request.VNPayPaymentRequest;
import com.choocapi.ecommercebackend.dto.response.VNPayPaymentResponse;
import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.enums.OrderStatus;
import com.choocapi.ecommercebackend.enums.PaymentStatus;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.repository.OrderRepository;
import com.choocapi.ecommercebackend.utils.VNPayUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VNPayService {
    
    VNPayConfig vnPayConfig;
    OrderRepository orderRepository;

    public VNPayPaymentResponse createPaymentUrl(VNPayPaymentRequest request, HttpServletRequest httpRequest) {
        try {
            // Find order
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

            // Generate transaction reference
            String vnpTxnRef = VNPayUtil.getRandomNumber(8);
            
            // Store vnpTxnRef in order for tracking
            order.setVnPayOrderId(vnpTxnRef);
            orderRepository.save(order);

            // Amount in smallest unit (multiply by 100)
            long amount = request.getAmount() * 100;

            // Build VNPay parameters
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(amount));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", vnpTxnRef);
            vnpParams.put("vnp_OrderInfo", "Thanh toan don hang: " + request.getOrderId());
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
            vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpRequest));

            // Create and expire dates (GMT+7)
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Build query string
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            
            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                        query.append('&');
                        hashData.append('&');
                    }
            }

            if (query.length() > 0)
                query.setLength(query.length() - 1);
            if (hashData.length() > 0)
                hashData.setLength(hashData.length() - 1);

            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnpSecretKey(), hashData.toString());
            query.append("&vnp_SecureHash=").append(vnpSecureHash);
            String paymentUrl = vnPayConfig.getVnpPayUrl() + "?" + query;

            return VNPayPaymentResponse.builder()
                    .code("00")
                    .message("success")
                    .paymentUrl(paymentUrl)
                    .build();

        } catch (UnsupportedEncodingException e) {
            log.error("Error creating VNPay payment URL", e);
            return VNPayPaymentResponse.builder()
                    .code("99")
                    .message("Error creating payment URL")
                    .build();
        }
    }

    @Transactional
    public Map<String, String> handlePaymentReturn(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();
        
        try {
            // Extract secure hash
            String vnpSecureHash = params.get("vnp_SecureHash");
            
            // Remove hash fields for validation
            Map<String, String> fields = new HashMap<>(params);
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");
            
            // Calculate secure hash
            String signValue = VNPayUtil.hashAllFields(fields, vnPayConfig.getVnpSecretKey());
            
            // Validate signature
            if (signValue.equals(vnpSecureHash)) {
                String vnpResponseCode = params.get("vnp_ResponseCode");
                String vnpTransactionStatus = params.get("vnp_TransactionStatus");
                String vnpTxnRef = params.get("vnp_TxnRef");
                String vnpAmount = params.get("vnp_Amount");
                
                // Find order by vnpTxnRef
                Order order = orderRepository.findByVnPayOrderId(vnpTxnRef)
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                
                // Validate amount (VNPay amount is in smallest unit, divide by 100)
                if (vnpAmount == null) {
                    result.put("status", "error");
                    result.put("message", "Missing amount");
                    return result;
                }
                
                long receivedAmount = Long.parseLong(vnpAmount) / 100;
                long orderAmount = order.getTotalAmount().longValue();
                if (orderAmount != receivedAmount) {
                    log.error("Invalid VNPay amount. Expected: {}, Got: {}", orderAmount, receivedAmount);
                    result.put("status", "error");
                    result.put("message", "Invalid amount");
                    return result;
                }
                
                if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
                    // Payment successful
                    order.setPaymentStatus(PaymentStatus.PAID);
                    order.setStatus(OrderStatus.PROCESSING);
                    orderRepository.save(order);
                    
                    result.put("status", "success");
                    result.put("message", "Payment successful");
                    result.put("orderId", order.getId());
                } else {
                    // Payment failed
                    order.setPaymentStatus(PaymentStatus.FAILED);
                    orderRepository.save(order);
                    
                    result.put("status", "failed");
                    result.put("message", "Payment failed");
                    result.put("orderId", order.getId());
                }
            } else {
                result.put("status", "error");
                result.put("message", "Invalid signature");
            }
        } catch (Exception e) {
            log.error("Error handling VNPay return", e);
            result.put("status", "error");
            result.put("message", "Error processing payment");
        }
        
        return result;
    }

}

