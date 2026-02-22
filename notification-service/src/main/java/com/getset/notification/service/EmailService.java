package com.getset.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@getset.com}")
    private String fromEmail;

    public void sendEnquiryReceivedEmail(String ownerEmail, String ownerName,
            String renterName, String propertyTitle, String message) {
        send(ownerEmail,
                "New Enquiry for " + propertyTitle,
                String.format(
                        "Hi %s,\n\nYou have a new enquiry from %s for your property '%s':\n\n\"%s\"\n\nLogin to GetSet to respond.\n\nGetSet Team",
                        ownerName, renterName, propertyTitle, message));
    }

    public void sendEnquiryAcceptedEmail(String renterEmail, String renterName,
            String ownerName, String propertyTitle) {
        send(renterEmail,
                "Your enquiry was accepted! 🎉",
                String.format(
                        "Hi %s,\n\nGreat news! %s has accepted your enquiry for '%s'.\n\nLogin to GetSet to view details.\n\nGetSet Team",
                        renterName, ownerName, propertyTitle));
    }

    public void sendEnquiryRejectedEmail(String renterEmail, String renterName,
            String ownerName, String propertyTitle, String reason) {
        send(renterEmail,
                "Update on your enquiry",
                String.format(
                        "Hi %s,\n\n%s has reviewed your enquiry for '%s'.\n\nReason: %s\n\nKeep exploring GetSet for other properties.\n\nGetSet Team",
                        renterName, ownerName, propertyTitle, reason != null ? reason : "Not specified"));
    }

    public void sendNewMessageEmail(String recipientEmail, String recipientName,
            String senderName, String preview) {
        send(recipientEmail,
                "New message from " + senderName,
                String.format("Hi %s,\n\n%s sent you a message:\n\n\"%s\"\n\nLogin to GetSet to reply.\n\nGetSet Team",
                        recipientName, senderName, preview));
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
