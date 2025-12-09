package com.choocapi.ecommercebackend.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.choocapi.ecommercebackend.configuration.ZalopayConfig;
import com.choocapi.ecommercebackend.dto.response.PaymentResponse;
import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.enums.OrderStatus;
import com.choocapi.ecommercebackend.enums.PaymentStatus;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.repository.OrderRepository;
import com.choocapi.ecommercebackend.utils.HMACUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ZalopayService {

	ZalopayConfig zalopayConfig;
	OrderRepository orderRepository;

	private static String getCurrentTimeString(String format) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		fmt.setCalendar(cal);
		return fmt.format(cal.getTimeInMillis());
	}

	public PaymentResponse createOrder(String orderId, Long amount) {
		try {
			// Find order
			Order order = orderRepository.findById(orderId)
					.orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

			Random rand = new Random();
			int randomId = rand.nextInt(1000000);

			// Generate app_trans_id: yyMMdd_randomId
			String appTransId = getCurrentTimeString("yyMMdd") + "_" + randomId;

			// Build embed_data with redirecturl to override merchant dashboard redirect
			// Note: This may not work if merchant dashboard has higher priority
			// Extract base URL from callback_url (e.g., http://localhost:8080/api/v1/zalopay/callback -> http://localhost:8080/api/v1)
			String callbackUrlValue = zalopayConfig.getCallbackUrl();
			String baseUrl = callbackUrlValue.substring(0, callbackUrlValue.lastIndexOf("/zalopay"));
			String returnUrl = baseUrl + "/zalopay/return";
			
			org.json.JSONObject embedData = new org.json.JSONObject();
			embedData.put("redirecturl", returnUrl);
			log.info("Setting ZaloPay redirect URL in embed_data: {}", returnUrl);

			Map<String, Object> orderData = new HashMap<>();
			orderData.put("app_id", zalopayConfig.getAppId());
			orderData.put("app_trans_id", appTransId);
			orderData.put("app_time", System.currentTimeMillis());
			orderData.put("app_user", "user123");
			orderData.put("amount", amount);
			orderData.put("description", "Thanh toan don hang: " + orderId);
			orderData.put("bank_code", "");
			orderData.put("item", "[{}]");
			orderData.put("embed_data", embedData.toString());
			orderData.put("callback_url", zalopayConfig.getCallbackUrl());

			// Build data string for MAC
			String data = orderData.get("app_id") + "|" + orderData.get("app_trans_id") + "|"
					+ orderData.get("app_user") + "|" + orderData.get("amount") + "|" + orderData.get("app_time")
					+ "|" + orderData.get("embed_data") + "|" + orderData.get("item");

			// Generate MAC
			String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zalopayConfig.getKey1(), data);
			orderData.put("mac", mac);

			log.info("Generated ZaloPay MAC: {}", mac);

			try (CloseableHttpClient client = HttpClients.createDefault()) {
				HttpPost post = new HttpPost(zalopayConfig.getEndpoint());

				List<NameValuePair> params = new java.util.ArrayList<>();
				for (Map.Entry<String, Object> entry : orderData.entrySet()) {
					params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
				}

				post.setEntity(new UrlEncodedFormEntity(params));

				try (CloseableHttpResponse response = client.execute(post)) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity().getContent()));
					StringBuilder resultJsonStr = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						resultJsonStr.append(line);
					}

					log.info("ZaloPay Response: {}", resultJsonStr.toString());

					JSONObject responseJson = new JSONObject(resultJsonStr.toString());
					int returnCode = responseJson.optInt("return_code", -1);
					String returnMessage = responseJson.optString("return_message", "");
					String orderUrl = responseJson.optString("order_url", "");

					if (returnCode == 1 && !orderUrl.isEmpty()) {
						// Store zalopayOrderId in order for tracking
						order.setZalopayOrderId(appTransId);
						orderRepository.save(order);

						return PaymentResponse.builder()
								.code("00")
								.message("success")
								.paymentUrl(orderUrl)
								.build();
					} else {
						log.error("ZaloPay order creation failed: {}", returnMessage);
						return PaymentResponse.builder()
								.code("99")
								.message(returnMessage)
								.build();
					}
				}
			}
		} catch (Exception e) {
			log.error("Error creating ZaloPay order", e);
			return PaymentResponse.builder()
					.code("99")
					.message("Failed to create order: " + e.getMessage())
					.build();
		}
	}

	@Transactional
	public Map<String, String> handleReturn(Map<String, String> params) {
		Map<String, String> result = new HashMap<>();
		try {
			String appId = params.get("appid");
			String appTransId = params.get("apptransid");
			String pmcId = params.get("pmcid");
			String bankCode = params.get("bankcode");
			String amount = params.get("amount");
			String discountAmount = params.get("discountamount");
			String status = params.get("status");
			String checksum = params.get("checksum");

			if (appTransId == null || checksum == null || status == null || amount == null) {
				result.put("status", "error");
				result.put("message", "Missing required parameters");
				return result;
			}

			// Validate checksum using key2 as described in ZaloPay docs
			String data = String.join("|", safe(appId), safe(appTransId), safe(pmcId), safe(bankCode), safe(amount),
					safe(discountAmount), safe(status));
			String calculatedChecksum = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zalopayConfig.getKey2(), data);

			if (!calculatedChecksum.equals(checksum)) {
				log.error("Invalid ZaloPay checksum. Expected: {}, Got: {}", calculatedChecksum, checksum);
				result.put("status", "error");
				result.put("message", "Invalid checksum");
				return result;
			}

			// Find order by zalopayOrderId
			Order order = orderRepository.findByZalopayOrderId(appTransId)
					.orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

			// Validate amount
			long receivedAmount = Long.parseLong(amount);
			if (order.getTotalAmount().longValue() != receivedAmount) {
				log.error("Invalid ZaloPay amount. Expected: {}, Got: {}", order.getTotalAmount(), receivedAmount);
				result.put("status", "error");
				result.put("message", "Invalid amount");
				return result;
			}

			// Handle status
			switch (status) {
			case "1":
				if (order.getPaymentStatus() != PaymentStatus.PAID) {
					order.setPaymentStatus(PaymentStatus.PAID);
					order.setStatus(OrderStatus.PROCESSING);
					orderRepository.save(order);
				}
				result.put("status", "success");
				result.put("message", "Payment successful");
				result.put("orderId", order.getId());
				break;
			case "-1":
				if (order.getPaymentStatus() != PaymentStatus.PAID) {
					order.setPaymentStatus(PaymentStatus.PENDING);
					orderRepository.save(order);
				}
				result.put("status", "processing");
				result.put("message", "Payment is processing");
				result.put("orderId", order.getId());
				break;
			default:
				if (order.getPaymentStatus() != PaymentStatus.PAID) {
					order.setPaymentStatus(PaymentStatus.FAILED);
					orderRepository.save(order);
				}
				result.put("status", "failed");
				result.put("message", "Payment failed with status: " + status);
				result.put("orderId", order.getId());
				break;
			}
		} catch (Exception e) {
			log.error("Error handling ZaloPay return", e);
			result.put("status", "error");
			result.put("message", "Error processing payment");
		}

		return result;
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}
}

