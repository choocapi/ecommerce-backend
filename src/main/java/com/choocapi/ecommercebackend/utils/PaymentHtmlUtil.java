package com.choocapi.ecommercebackend.utils;

public class PaymentHtmlUtil {

	/**
	 * Generate HTML page for payment redirect with loading spinner
	 * 
	 * @param redirectUrl The URL to redirect to
	 * @return HTML string with meta refresh and JavaScript redirect
	 */
	public static String generateRedirectHtml(String redirectUrl) {
		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <meta http-equiv="refresh" content="0;url=%s">
				    <title>Đang xử lý thanh toán...</title>
				    <style>
				        body {
				            font-family: Arial, sans-serif;
				            display: flex;
				            justify-content: center;
				            align-items: center;
				            height: 100vh;
				            margin: 0;
				            background: linear-gradient(135deg, #43e97b 0%%, #38f9d7 100%%);
				            color: white;
				        }
				        .container {
				            text-align: center;
				        }
				        .spinner {
				            border: 4px solid rgba(255, 255, 255, 0.3);
				            border-radius: 50%%;
				            border-top: 4px solid white;
				            width: 40px;
				            height: 40px;
				            animation: spin 1s linear infinite;
				            margin: 0 auto 20px;
				        }
				        @keyframes spin {
				            0%% { transform: rotate(0deg); }
				            100%% { transform: rotate(360deg); }
				        }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="spinner"></div>
				        <h2>Đang xử lý kết quả thanh toán...</h2>
				        <p>Vui lòng chờ trong giây lát</p>
				    </div>
				    <script>
				        window.location.href = '%s';
				    </script>
				</body>
				</html>
				""".formatted(redirectUrl, redirectUrl);
	}
}

