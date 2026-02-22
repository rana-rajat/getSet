package com.getset.notification;

import com.getset.common.NotFoundException;
import com.getset.common.PageResponse;
import com.getset.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

        private final NotificationRepository notificationRepository;
        private final EmailService emailService;

        @Override
        public void notifyEnquiryReceived(String ownerEmail, String ownerName, String renterId, String renterName,
                        String propertyId, String propertyTitle, String message) {
                log.info("Sending enquiry received notification to owner: {}", ownerEmail);

                // Send email
                emailService.sendEnquiryReceivedEmail(ownerEmail, ownerName, renterName, propertyTitle);

                // Create notification document (would be fetched from context - using dummy
                // ownerId for now)
                NotificationDocument notification = NotificationDocument.builder()
                                .recipientId(ownerEmail) // In real scenario, use actual ownerID
                                .recipientEmail(ownerEmail)
                                .subject("New Enquiry Received - " + propertyTitle)
                                .body(renterName + " has expressed interest in your property: " + propertyTitle)
                                .type("ENQUIRY_RECEIVED")
                                .relatedEntityId(propertyId)
                                .emailSent(true)
                                .build();

                notificationRepository.save(notification);
        }

        @Override
        public void notifyEnquiryAccepted(String renterEmail, String renterName, String ownerName,
                        String propertyId, String propertyTitle) {
                log.info("Sending enquiry accepted notification to renter: {}", renterEmail);

                // Send email
                emailService.sendEnquiryAcceptedEmail(renterEmail, renterName, ownerName, propertyTitle);

                // Create notification document
                NotificationDocument notification = NotificationDocument.builder()
                                .recipientEmail(renterEmail)
                                .subject("Your Enquiry Has Been Accepted - " + propertyTitle)
                                .body(ownerName + " has accepted your enquiry for " + propertyTitle)
                                .type("ENQUIRY_ACCEPTED")
                                .relatedEntityId(propertyId)
                                .emailSent(true)
                                .build();

                notificationRepository.save(notification);
        }

        @Override
        public void notifyEnquiryRejected(String renterEmail, String renterName, String ownerName,
                        String propertyId, String propertyTitle, String reason) {
                log.info("Sending enquiry rejected notification to renter: {}", renterEmail);

                // Send email
                emailService.sendEnquiryRejectedEmail(renterEmail, renterName, ownerName, propertyTitle, reason);

                // Create notification document
                NotificationDocument notification = NotificationDocument.builder()
                                .recipientEmail(renterEmail)
                                .subject("Your Enquiry Has Been Declined - " + propertyTitle)
                                .body(ownerName + " has declined your enquiry for " + propertyTitle)
                                .type("ENQUIRY_REJECTED")
                                .relatedEntityId(propertyId)
                                .emailSent(true)
                                .emailSentError(reason)
                                .build();

                notificationRepository.save(notification);
        }

        @Override
        public void notifyNewMessage(String recipientId, String recipientEmail, String recipientName,
                        String senderName, String messagePreview) {
                log.info("Sending new message notification to: {}", recipientEmail);

                String subject = "New Message from " + senderName;
                String body = String.format(
                                "Hi %s,\n\n" +
                                                "%s has sent you a message:\n\n" +
                                                "\"%s\"\n\n" +
                                                "Log in to GetSet to reply.\n\n" +
                                                "Best regards,\n" +
                                                "GetSet Team",
                                recipientName, senderName, messagePreview);

                // Send email
                emailService.sendEmail(recipientEmail, subject, body);

                // Create notification document
                NotificationDocument notification = NotificationDocument.builder()
                                .recipientId(recipientId)
                                .recipientEmail(recipientEmail)
                                .subject(subject)
                                .body(body)
                                .type("MESSAGE_RECEIVED")
                                .emailSent(true)
                                .build();

                notificationRepository.save(notification);
        }

        @Override
        public PageResponse<NotificationResponse> getNotifications(String userId, Pageable pageable) {
                log.info("Fetching notifications for user: {}", userId);

                Page<NotificationDocument> page = notificationRepository.findByRecipientId(userId, pageable);

                var content = page.getContent().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return PageResponse.<NotificationResponse>builder()
                                .content(content)
                                .pageNumber(page.getNumber())
                                .pageSize(page.getSize())
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .hasNext(!page.isLast())
                                .hasPrevious(page.getNumber() > 0)
                                .build();
        }

        @Override
        public PageResponse<NotificationResponse> getUnreadNotifications(String userId, Pageable pageable) {
                log.info("Fetching unread notifications for user: {}", userId);

                Page<NotificationDocument> page = notificationRepository.findByRecipientIdAndRead(userId, false,
                                pageable);

                var content = page.getContent().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return PageResponse.<NotificationResponse>builder()
                                .content(content)
                                .pageNumber(page.getNumber())
                                .pageSize(page.getSize())
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .hasNext(!page.isLast())
                                .hasPrevious(page.getNumber() > 0)
                                .build();
        }

        @Override
        @SuppressWarnings("null")
        public void markAsRead(String notificationId) {
                log.info("Marking notification {} as read", notificationId);

                NotificationDocument notification = notificationRepository.findById(notificationId)
                                .orElseThrow(() -> new NotFoundException("Notification not found"));

                notification.setRead(true);
                notificationRepository.save(notification);
        }

        @Override
        public void markAllAsRead(String userId) {
                log.info("Marking all notifications as read for user: {}", userId);

                Page<NotificationDocument> unreadNotifications = notificationRepository
                                .findByRecipientIdAndRead(userId, false,
                                                org.springframework.data.domain.Pageable.unpaged());

                unreadNotifications.getContent().forEach(notification -> {
                        notification.setRead(true);
                        notificationRepository.save(notification);
                });
        }

        @Override
        public long getUnreadCount(String userId) {
                return notificationRepository.countByRecipientIdAndRead(userId, false);
        }

        @Override
        @SuppressWarnings("null")
        public void deleteNotification(String notificationId) {
                log.info("Deleting notification: {}", notificationId);

                if (!notificationRepository.existsById(notificationId)) {
                        throw new NotFoundException("Notification not found");
                }

                notificationRepository.deleteById(notificationId);
        }

        private NotificationResponse mapToResponse(NotificationDocument notification) {
                return NotificationResponse.builder()
                                .id(notification.getId())
                                .subject(notification.getSubject())
                                .body(notification.getBody())
                                .type(notification.getType())
                                .relatedEntityId(notification.getRelatedEntityId())
                                .read(notification.isRead())
                                .emailSent(notification.isEmailSent())
                                .createdAt(notification.getCreatedAt())
                                .build();
        }
}
