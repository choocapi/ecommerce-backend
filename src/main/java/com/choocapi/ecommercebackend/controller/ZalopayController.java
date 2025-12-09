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

import com.choocapi.ecommercebackend.configuration.ZalopayConfig;
import com.choocapi.ecommercebackend.dto.request.PaymentRequest;
import com.choocapi.ecommercebackend.dto.response.PaymentResponse;
import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.repository.OrderRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;
import com.choocapi.ecommercebackend.service.ZalopayService;
import com.choocapi.ecommercebackend.utils.PaymentHtmlUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/zalopay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ZalopayController {

	ZalopayService zalopayService;
	OrderRepository orderRepository;
	UserRepository userRepository;
	ZalopayConfig zalopayConfig;

	@PostMapping("/create")
	public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
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

		return zalopayService.createOrder(request.getOrderId(), request.getAmount());
	}

	@GetMapping("/return")
	public String paymentReturn(@RequestParam Map<String, String> params, HttpServletResponse response)
			throws IOException {
		log.info("ZaloPay return (callback merged) with params: {}", params);

		Map<String, String> result = zalopayService.handleReturn(params);
		String status = result.getOrDefault("status", "error");
		String orderId = result.get("orderId");
		String redirectUrl;

		switch (status) {
		case "success":
			redirectUrl = zalopayConfig.getFrontendUrl() + "/checkout/result?status=success&orderId=" + orderId;
			break;
		case "failed":
			redirectUrl = zalopayConfig.getFrontendUrl() + "/checkout/result?status=failed"
					+ (orderId != null ? "&orderId=" + orderId : "");
			break;
		case "processing":
			redirectUrl = zalopayConfig.getFrontendUrl() + "/checkout/result?status=pending"
					+ (orderId != null ? "&orderId=" + orderId : "");
			break;
		default:
			redirectUrl = zalopayConfig.getFrontendUrl() + "/checkout/result?status=error";
		}

		log.info("Redirecting to: {}", redirectUrl);

		response.setContentType("text/html; charset=UTF-8");
		return PaymentHtmlUtil.generateRedirectHtml(redirectUrl);
	}
}

