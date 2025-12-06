package com.getset.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public boolean sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@getset.com");
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return false;
        }
    }
    
    public void sendEnquiryReceivedEmail(String ownerEmail, String ownerName, String renterName, String propertyTitle) {
        String subject = "New Enquiry Received - " + propertyTitle;
        String body = String.format(
                "Hi %s,\n\n" +
                "%s has expressed interest in your property: %s\n\n" +
                "Please log in to your GetSet account to view and respond to this enquiry.\n\n" +
                "Best regards,\n" +
                "GetSet Team",
                ownerName, renterName, propertyTitle
        );
        sendEmail(ownerEmail, subject, body);
    }
    
    public void sendEnquiryAcceptedEmail(String renterEmail, String renterName, String ownerName, String propertyTitle) {
        String subject = "Your Enquiry Has Been Accepted - " + propertyTitle;
        String body = String.format(
                "Hi %s,\n\n" +
                "%s has accepted your enquiry for %s!\n\n" +
                "You can now contact the owner to discuss further details.\n\n" +
                "Best regards,\n" +
                "GetSet Team",
                renterName, ownerName, propertyTitle
        );
        sendEmail(renterEmail, subject, body);
    }
    
    public void sendEnquiryRejectedEmail(String renterEmail, String renterName, String ownerName, String propertyTitle, String reason) {
        String subject = "Your Enquiry Has Been Declined - " + propertyTitle;
        String body = String.format(
                "Hi %s,\n\n" +
                "%s has declined your enquiry for %s.\n" +
                "Reason: %s\n\n" +
                "Please feel free to explore other properties on GetSet.\n\n" +
                "Best regards,\n" +
                "GetSet Team",
                renterName, ownerName, propertyTitle, reason != null ? reason : "Not specified"
        );
        sendEmail(renterEmail, subject, body);
    }
}
