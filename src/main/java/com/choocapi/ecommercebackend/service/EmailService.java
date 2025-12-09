package com.choocapi.ecommercebackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    JavaMailSender mailSender;

    @NonFinal
    @Value("${spring.mail.username}")
    String from;

    public void sendVerificationEmail(User user, String tokenUrl) {
        String subject = "Verify your email";
        String body = "<p>Hello " + user.getFirstName() + ",</p>"
                + "<p>Please verify your email by clicking the link below:</p>"
                + "<p><a href='" + tokenUrl + "'>Verify Email</a></p>";
        sendHtml(user.getEmail(), subject, body);
    }

    public void sendResetPasswordEmail(User user, String tokenUrl) {
        String subject = "Reset your password";
        String body = "<p>Hello " + user.getFirstName() + ",</p>"
                + "<p>Reset your password using the link below:</p>"
                + "<p><a href='" + tokenUrl + "'>Reset Password</a></p>";
        sendHtml(user.getEmail(), subject, body);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}


