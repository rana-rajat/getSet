package com.getset.notification;

import com.getset.common.PageResponse;
import com.getset.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * Get all notifications for current user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<NotificationResponse> response = notificationService.getNotifications(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<NotificationResponse>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<NotificationResponse> response = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get unread count
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUnreadCount(
            @RequestHeader("X-User-Id") String userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(java.util.Map.of("unreadCount", count));
    }
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-Id") String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
