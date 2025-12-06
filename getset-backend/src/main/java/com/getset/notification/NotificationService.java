package com.getset.notification;

import com.getset.common.PageResponse;
import com.getset.notification.dto.NotificationResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    
    /**
     * Create and send an enquiry received notification
     */
    void notifyEnquiryReceived(String ownerEmail, String ownerName, String renterId, String renterName, String propertyId, String propertyTitle, String message);
    
    /**
     * Create and send an enquiry accepted notification
     */
    void notifyEnquiryAccepted(String renterEmail, String renterName, String ownerName, String propertyId, String propertyTitle);
    
    /**
     * Create and send an enquiry rejected notification
     */
    void notifyEnquiryRejected(String renterEmail, String renterName, String ownerName, String propertyId, String propertyTitle, String reason);
    
    /**
     * Create and send a new message notification
     */
    void notifyNewMessage(String recipientId, String recipientEmail, String recipientName, String senderName, String messagePreview);
    
    /**
     * Get user notifications
     */
    PageResponse<NotificationResponse> getNotifications(String userId, Pageable pageable);
    
    /**
     * Get unread notifications
     */
    PageResponse<NotificationResponse> getUnreadNotifications(String userId, Pageable pageable);
    
    /**
     * Mark notification as read
     */
    void markAsRead(String notificationId);
    
    /**
     * Mark all notifications as read
     */
    void markAllAsRead(String userId);
    
    /**
     * Get unread count
     */
    long getUnreadCount(String userId);
    
    /**
     * Delete notification
     */
    void deleteNotification(String notificationId);
}
