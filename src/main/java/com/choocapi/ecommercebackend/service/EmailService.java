package com.choocapi.ecommercebackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.entity.User;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class EmailService {
    final Resend resend;

    @NonFinal
    @Value("${resend.from-email}")
    String fromEmail;

    public EmailService(@Value("${resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    @Async
    public void sendVerificationEmail(User user, String tokenUrl) {
        String subject = "Xác thực email";
        String html = "<p>Xin chào " + user.getFirstName() + ",</p>"
                + "<p>Vui lòng xác thực email bằng cách nhấp vào liên kết bên dưới:</p>"
                + "<p><a href='" + tokenUrl + "'>Xác thực email</a></p>";
        sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendResetPasswordEmail(User user, String tokenUrl) {
        String subject = "Đặt lại mật khẩu";
        String html = "<p>Xin chào " + user.getFirstName() + ",</p>"
                + "<p>Đặt lại mật khẩu bằng cách nhấp vào liên kết bên dưới:</p>"
                + "<p><a href='" + tokenUrl + "'>Đặt lại mật khẩu</a></p>";
        sendHtml(user.getEmail(), subject, html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email sent successfully to {} with ID: {}", to, response.getId());
        } catch (ResendException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email via Resend", e);
        }
    }
}


