package com.getset.enquiry;

import com.getset.common.NotFoundException;
import com.getset.common.PageResponse;
import com.getset.enquiry.dto.EnquiryRequest;
import com.getset.enquiry.dto.EnquiryResponse;
import com.getset.enquiry.dto.EnquiryUpdateRequest;
import com.getset.events.NotificationEvent;
import com.getset.events.NotificationEventPublisher;
import com.getset.property.PropertyDocument;
import com.getset.property.PropertyRepository;
import com.getset.user.UserDocument;
import com.getset.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnquiryServiceImpl implements EnquiryService {

        private final EnquiryRepository enquiryRepository;
        private final PropertyRepository propertyRepository;
        private final UserRepository userRepository;
        private final NotificationEventPublisher eventPublisher;

        @Override
        @Transactional
        public EnquiryResponse createEnquiry(EnquiryRequest request, String renterId) {
                log.info("Creating enquiry for property {} by renter {}", request.getPropertyId(), renterId);

                // Verify property exists
                propertyRepository.findById(request.getPropertyId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Property not found with ID: " + request.getPropertyId()));

                // Get renter info
                UserDocument renter = userRepository.findById(renterId)
                                .orElseThrow(() -> new NotFoundException("Renter not found"));

                // Get property for notification context
                PropertyDocument property = propertyRepository.findById(request.getPropertyId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Property not found with ID: " + request.getPropertyId()));

                // Create enquiry
                EnquiryDocument enquiry = EnquiryDocument.builder()
                                .propertyId(request.getPropertyId())
                                .ownerId(property.getOwnerId())
                                .renterId(renterId)
                                .message(request.getMessage())
                                .status(EnquiryStatus.PENDING)
                                .build();

                EnquiryDocument saved = enquiryRepository.save(enquiry);
                log.info("Enquiry created with ID: {}", saved.getId());

                // Publish async notification event (does NOT block the HTTP response)
                UserDocument owner = userRepository.findById(property.getOwnerId()).orElse(null);
                if (owner != null) {
                        eventPublisher.publish(NotificationEvent.builder()
                                        .eventType(NotificationEvent.EventType.ENQUIRY_RECEIVED)
                                        .recipientId(owner.getId())
                                        .recipientEmail(owner.getEmail())
                                        .recipientName(owner.getName())
                                        .actorName(renter.getName())
                                        .propertyId(property.getId())
                                        .propertyTitle(property.getTitle())
                                        .extraPayload(request.getMessage())
                                        .relatedEntityId(saved.getId())
                                        .build());
                }

                return mapToResponse(saved, renter, null);
        }

        @Override
        public EnquiryResponse getEnquiryById(String id) {
                log.info("Fetching enquiry with ID: {}", id);

                EnquiryDocument enquiry = enquiryRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Enquiry not found with ID: " + id));

                UserDocument renter = userRepository.findById(enquiry.getRenterId())
                                .orElseThrow(() -> new NotFoundException("Renter not found"));

                UserDocument owner = userRepository.findById(enquiry.getOwnerId())
                                .orElseThrow(() -> new NotFoundException("Owner not found"));

                return mapToResponse(enquiry, renter, owner);
        }

        @Override
        public PageResponse<EnquiryResponse> getEnquiriesByPropertyId(String propertyId, String ownerId,
                        Pageable pageable) {
                log.info("Fetching enquiries for property {} by owner {}", propertyId, ownerId);

                // Verify owner owns this property
                propertyRepository.findByIdAndOwnerId(propertyId, ownerId)
                                .orElseThrow(() -> new NotFoundException("You don't have access to this property"));

                Page<EnquiryDocument> page = enquiryRepository.findByPropertyId(propertyId, pageable);

                var content = page.getContent().stream()
                                .map(enquiry -> {
                                        UserDocument renter = userRepository.findById(enquiry.getRenterId())
                                                        .orElseThrow(() -> new NotFoundException("Renter not found"));
                                        return mapToResponse(enquiry, renter, null);
                                })
                                .collect(Collectors.toList());

                return PageResponse.<EnquiryResponse>builder()
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
        public PageResponse<EnquiryResponse> getEnquiriesByRenterId(String renterId, Pageable pageable) {
                log.info("Fetching enquiries by renter {}", renterId);

                Page<EnquiryDocument> page = enquiryRepository.findByRenterId(renterId, pageable);

                var content = page.getContent().stream()
                                .map(enquiry -> {
                                        UserDocument renterUser = userRepository.findById(enquiry.getRenterId())
                                                        .orElseThrow(() -> new NotFoundException("Renter not found"));
                                        UserDocument ownerUser = userRepository.findById(enquiry.getOwnerId())
                                                        .orElseThrow(() -> new NotFoundException("Owner not found"));
                                        return mapToResponse(enquiry, renterUser, ownerUser);
                                })
                                .collect(Collectors.toList());

                return PageResponse.<EnquiryResponse>builder()
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
        @Transactional
        public EnquiryResponse updateEnquiry(String id, EnquiryUpdateRequest request, String ownerId) {
                log.info("Updating enquiry {} by owner {}", id, ownerId);

                EnquiryDocument enquiry = enquiryRepository.findByIdAndOwnerId(id, ownerId)
                                .orElseThrow(() -> new NotFoundException("Enquiry not found or you don't have access"));

                // Update status
                EnquiryStatus newStatus = EnquiryStatus.valueOf(request.getStatus().toUpperCase());
                enquiry.setStatus(newStatus);
                enquiry.setRejectionReason(request.getRejectionReason());
                enquiry.setUpdatedAt(Instant.now());

                EnquiryDocument updated = enquiryRepository.save(enquiry);
                log.info("Enquiry {} updated with status {}", id, newStatus);

                UserDocument renter = userRepository.findById(updated.getRenterId())
                                .orElseThrow(() -> new NotFoundException("Renter not found"));

                UserDocument owner = userRepository.findById(ownerId)
                                .orElseThrow(() -> new NotFoundException("Owner not found"));

                // Resolve property title for notification
                PropertyDocument property = propertyRepository.findById(updated.getPropertyId()).orElse(null);
                String propertyTitle = property != null ? property.getTitle() : "your property";

                // Publish async notification event
                NotificationEvent.EventType notifType = newStatus == EnquiryStatus.ACCEPTED
                                ? NotificationEvent.EventType.ENQUIRY_ACCEPTED
                                : NotificationEvent.EventType.ENQUIRY_REJECTED;

                eventPublisher.publish(NotificationEvent.builder()
                                .eventType(notifType)
                                .recipientId(renter.getId())
                                .recipientEmail(renter.getEmail())
                                .recipientName(renter.getName())
                                .actorName(owner.getName())
                                .propertyId(updated.getPropertyId())
                                .propertyTitle(propertyTitle)
                                .extraPayload(request.getRejectionReason())
                                .relatedEntityId(updated.getId())
                                .build());

                return mapToResponse(updated, renter, owner);
        }

        @Override
        public void deleteEnquiry(String id, String userId) {
                log.info("Deleting enquiry {} by user {}", id, userId);

                EnquiryDocument enquiry = enquiryRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Enquiry not found"));

                // Only renter or owner can delete
                if (!enquiry.getRenterId().equals(userId) && !enquiry.getOwnerId().equals(userId)) {
                        throw new IllegalArgumentException("You don't have permission to delete this enquiry");
                }

                enquiryRepository.deleteById(id);
                log.info("Enquiry {} deleted", id);
        }

        @Override
        public Object getEnquiryStats(String ownerId) {
                log.info("Fetching enquiry stats for owner {}", ownerId);

                long totalEnquiries = enquiryRepository.countByOwnerId(ownerId);
                long pendingEnquiries = enquiryRepository.countByOwnerIdAndStatus(ownerId, EnquiryStatus.PENDING);
                long acceptedEnquiries = enquiryRepository.countByOwnerIdAndStatus(ownerId, EnquiryStatus.ACCEPTED);
                long rejectedEnquiries = enquiryRepository.countByOwnerIdAndStatus(ownerId, EnquiryStatus.REJECTED);

                Map<String, Object> stats = new HashMap<>();
                stats.put("totalEnquiries", totalEnquiries);
                stats.put("pendingEnquiries", pendingEnquiries);
                stats.put("acceptedEnquiries", acceptedEnquiries);
                stats.put("rejectedEnquiries", rejectedEnquiries);

                return stats;
        }

        private EnquiryResponse mapToResponse(EnquiryDocument enquiry, UserDocument renter, UserDocument owner) {
                return EnquiryResponse.builder()
                                .id(enquiry.getId())
                                .propertyId(enquiry.getPropertyId())
                                .renterId(enquiry.getRenterId())
                                .renterName(renter.getName())
                                .renterEmail(renter.getEmail())
                                .renterPhone(renter.getPhone())
                                .ownerId(enquiry.getOwnerId())
                                .ownerName(owner != null ? owner.getName() : "N/A")
                                .message(enquiry.getMessage())
                                .status(enquiry.getStatus())
                                .rejectionReason(enquiry.getRejectionReason())
                                .createdAt(enquiry.getCreatedAt())
                                .updatedAt(enquiry.getUpdatedAt())
                                .build();
        }
}
