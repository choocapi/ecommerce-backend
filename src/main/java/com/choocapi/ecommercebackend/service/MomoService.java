package com.choocapi.ecommercebackend.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.choocapi.ecommercebackend.configuration.MomoConfig;
import com.choocapi.ecommercebackend.dto.response.PaymentResponse;
import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.enums.OrderStatus;
import com.choocapi.ecommercebackend.enums.PaymentStatus;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.repository.OrderRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MomoService {

	MomoConfig momoConfig;
	OrderRepository orderRepository;

	public PaymentResponse createPaymentRequest(String orderId, Long amount) {
		try {
			// Find order
			Order order = orderRepository.findById(orderId)
					.orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

			// Generate requestId and orderId for MoMo
			String requestId = momoConfig.getPartnerCode() + new Date().getTime();
			String momoOrderId = requestId;
			String orderInfo = "Thanh toan don hang: " + orderId;
			String extraData = "";

			// Generate raw signature
			String rawSignature = String.format(
					"accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
					momoConfig.getAccessKey(), amount, extraData, momoConfig.getIpnUrl(), momoOrderId, orderInfo,
					momoConfig.getPartnerCode(), momoConfig.getRedirectUrl(), requestId,
					momoConfig.getRequestType());

			// Sign with HMAC SHA256
			String signature = signHmacSHA256(rawSignature, momoConfig.getSecretKey());
			log.info("Generated MoMo Signature: {}", signature);

			JSONObject requestBody = new JSONObject();
			requestBody.put("partnerCode", momoConfig.getPartnerCode());
			requestBody.put("accessKey", momoConfig.getAccessKey());
			requestBody.put("requestId", requestId);
			requestBody.put("amount", amount);
			requestBody.put("orderId", momoOrderId);
			requestBody.put("orderInfo", orderInfo);
			requestBody.put("redirectUrl", momoConfig.getRedirectUrl());
			requestBody.put("ipnUrl", momoConfig.getIpnUrl());
			requestBody.put("extraData", extraData);
			requestBody.put("requestType", momoConfig.getRequestType());
			requestBody.put("signature", signature);
			requestBody.put("lang", "en");

			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(momoConfig.getApiUrl());
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

			try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
				StringBuilder result = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					result.append(line);
				}
				log.info("Response from MoMo: {}", result.toString());

				JSONObject responseJson = new JSONObject(result.toString());
				String resultCode = responseJson.optString("resultCode", "99");
				String payUrl = responseJson.optString("payUrl", "");

				if ("0".equals(resultCode) && !payUrl.isEmpty()) {
					// Store momoOrderId in order for tracking
					order.setMomoOrderId(momoOrderId);
					orderRepository.save(order);

					return PaymentResponse.builder()
							.code("00")
							.message("success")
							.paymentUrl(payUrl)
							.build();
				} else {
					String message = responseJson.optString("message", "Failed to create payment");
					log.error("MoMo payment creation failed: {}", message);
					return PaymentResponse.builder()
							.code("99")
							.message(message)
							.build();
				}
			}
		} catch (Exception e) {
			log.error("Error creating MoMo payment request", e);
			return PaymentResponse.builder()
					.code("99")
					.message("Failed to create payment request: " + e.getMessage())
					.build();
		}
	}

	// HMAC SHA256 signing method
	private static String signHmacSHA256(String data, String key) throws Exception {
		Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		hmacSHA256.init(secretKey);
		byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
		StringBuilder hexString = new StringBuilder();
		for (byte b : hash) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	@Transactional
	public Map<String, String> handleReturn(Map<String, String> params) {
		Map<String, String> result = new java.util.HashMap<>();
		try {
			String orderId = params.get("orderId");
			String resultCode = params.get("resultCode");
			String amount = params.get("amount");
			String signature = params.get("signature");

			// Find order by momoOrderId
			Order order = orderRepository.findByMomoOrderId(orderId)
					.orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

			// Validate signature
			// Note: accessKey is not in params, use from config instead
			// Handle null values by using empty string
			String rawSignature = String.format(
					"accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
					momoConfig.getAccessKey(), 
					params.getOrDefault("amount", ""), 
					params.getOrDefault("extraData", ""), 
					params.getOrDefault("message", ""),
					params.getOrDefault("orderId", ""), 
					params.getOrDefault("orderInfo", ""), 
					params.getOrDefault("orderType", ""),
					params.getOrDefault("partnerCode", ""), 
					params.getOrDefault("payType", ""), 
					params.getOrDefault("requestId", ""),
					params.getOrDefault("responseTime", ""), 
					params.getOrDefault("resultCode", ""), 
					params.getOrDefault("transId", ""));

			String calculatedSignature = signHmacSHA256(rawSignature, momoConfig.getSecretKey());
			
			log.debug("MoMo signature validation - Raw signature string: {}", rawSignature);
			log.debug("MoMo signature validation - Calculated: {}, Received: {}", calculatedSignature, signature);

			if (!calculatedSignature.equals(signature)) {
				log.error("Invalid MoMo signature. Expected: {}, Got: {}", calculatedSignature, signature);
				result.put("status", "error");
				result.put("message", "Invalid signature");
				return result;
			}

			// Validate amount
			long receivedAmount = Long.parseLong(amount);
			if (order.getTotalAmount().longValue() != receivedAmount) {
				log.error("Invalid MoMo amount. Expected: {}, Got: {}", order.getTotalAmount(), receivedAmount);
				result.put("status", "error");
				result.put("message", "Invalid amount");
				return result;
			}

			// Check if already processed
			if (order.getPaymentStatus() == PaymentStatus.PAID) {
				result.put("status", "success");
				result.put("message", "Order already confirmed");
				result.put("orderId", order.getId());
				return result;
			}

			// Process payment status
			if ("0".equals(resultCode)) {
				order.setPaymentStatus(PaymentStatus.PAID);
				order.setStatus(OrderStatus.PROCESSING);
				orderRepository.save(order);

				result.put("status", "success");
				result.put("message", "Payment successful");
				result.put("orderId", order.getId());
			} else if ("9000".equals(resultCode)) {
				// Processing
				if (order.getPaymentStatus() != PaymentStatus.PAID) {
					order.setPaymentStatus(PaymentStatus.PENDING);
					orderRepository.save(order);
				}

				result.put("status", "processing");
				result.put("message", "Payment is processing");
				result.put("orderId", order.getId());
			} else {
				// Payment failed
				if (order.getPaymentStatus() != PaymentStatus.PAID) {
					order.setPaymentStatus(PaymentStatus.FAILED);
					orderRepository.save(order);
				}

				result.put("status", "failed");
				result.put("message", "Payment failed");
				result.put("orderId", order.getId());
			}
		} catch (Exception e) {
			log.error("Error handling MoMo callback", e);
			result.put("status", "error");
			result.put("message", "Error processing payment");
		}

		return result;
	}
}

