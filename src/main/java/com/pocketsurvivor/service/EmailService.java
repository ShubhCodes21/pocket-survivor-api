package com.pocketsurvivor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Async
    public void sendPasswordResetEmail(String toEmail, String otp) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Pocket Survivor - Password Reset Code");
            helper.setText(buildResetEmailHtml(otp), true);

            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildResetEmailHtml(String otp) {
        return """
            <div style="font-family:'Nunito',Arial,sans-serif;max-width:480px;margin:0 auto;padding:32px;background:#FFF9F5;border-radius:20px">
              <div style="text-align:center;margin-bottom:24px">
                <span style="font-size:48px">💸</span>
                <h1 style="font-family:'Outfit',Arial,sans-serif;color:#FF6348;font-size:24px;margin:8px 0">Pocket Survivor</h1>
              </div>
              <div style="background:white;border-radius:16px;padding:24px;box-shadow:0 2px 12px rgba(0,0,0,0.06)">
                <p style="color:#222F3E;font-size:16px;margin-bottom:16px">You requested a password reset. Use this code:</p>
                <div style="text-align:center;margin:24px 0">
                  <span style="font-family:'Outfit',monospace;font-size:36px;font-weight:800;letter-spacing:8px;color:#FF6348;background:#FFF0EB;padding:12px 24px;border-radius:12px">%s</span>
                </div>
                <p style="color:#8395A7;font-size:14px;margin-top:16px">This code expires in <strong>15 minutes</strong>.</p>
                <p style="color:#8395A7;font-size:14px">If you didn't request this, just ignore this email.</p>
              </div>
              <p style="text-align:center;color:#8395A7;font-size:12px;margin-top:24px">Pocket Survivor - Your savage spending coach</p>
            </div>
            """.formatted(otp);
    }
}
