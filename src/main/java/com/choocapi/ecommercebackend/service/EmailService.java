package com.choocapi.ecommercebackend.service;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    JavaMailSender mailSender;

    @NonFinal
    @Value("${spring.mail.username}")
    String from;

    @Async
    public void sendVerificationEmail(User user, String tokenUrl) {
        String subject = "Xác thực email";
        String body = "<p>Xin chào " + user.getFirstName() + ",</p>"
                + "<p>Vui lòng xác thực email bằng cách nhấp vào liên kết bên dưới:</p>"
                + "<p><a href='" + tokenUrl + "'>Xác thực email</a></p>";
        sendHtml(user.getEmail(), subject, body);
    }

    @Async
    public void sendResetPasswordEmail(User user, String tokenUrl) {
        String subject = "Đặt lại mật khẩu";
        String body = "<p>Xin chào " + user.getFirstName() + ",</p>"
                + "<p>Đặt lại mật khẩu bằng cách nhấp vào liên kết bên dưới:</p>"
                + "<p><a href='" + tokenUrl + "'>Đặt lại mật khẩu</a></p>";
        sendHtml(user.getEmail(), subject, body);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            // Ensure reasonable SMTP timeouts to avoid blocking request threads
            if (mailSender instanceof JavaMailSenderImpl senderImpl) {
                Properties props = senderImpl.getJavaMailProperties();
                props.putIfAbsent("mail.smtp.connectiontimeout", "5000"); // 5s connect
                props.putIfAbsent("mail.smtp.timeout", "10000"); // 10s read
                props.putIfAbsent("mail.smtp.writetimeout", "10000"); // 10s write
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}


