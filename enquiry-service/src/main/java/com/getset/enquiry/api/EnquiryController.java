package com.getset.enquiry.api;

import com.getset.common.dto.PageResponse;
import com.getset.common.dto.PropertySummaryDto;
import com.getset.common.dto.UserSummaryDto;
import com.getset.common.event.NotificationEvent;
import com.getset.common.exception.ForbiddenException;
import com.getset.common.exception.NotFoundException;
import com.getset.enquiry.client.PropertyServiceClient;
import com.getset.enquiry.client.UserServiceClient;
import com.getset.enquiry.domain.EnquiryDocument;
import com.getset.enquiry.domain.EnquiryRepository;
import com.getset.enquiry.domain.EnquiryStatus;
import com.getset.enquiry.dto.EnquiryRequest;
import com.getset.enquiry.dto.EnquiryUpdateRequest;
import com.getset.enquiry.event.NotificationEventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final EnquiryRepository enquiryRepository;
    private final UserServiceClient userServiceClient;
    private final PropertyServiceClient propertyServiceClient;
    private final NotificationEventPublisher eventPublisher;

    @PostMapping
    public ResponseEntity<EnquiryDocument> create(@Valid @RequestBody EnquiryRequest req,
            Principal principal) {
        String renterId = principal.getName();
        UserSummaryDto renter = userServiceClient.getUserById(renterId);
        PropertySummaryDto property = propertyServiceClient.getPropertyById(req.getPropertyId());
        UserSummaryDto owner = userServiceClient.getUserById(property.getOwnerId());

        EnquiryDocument enquiry = EnquiryDocument.builder()
                .propertyId(property.getId()).propertyTitle(property.getTitle())
                .ownerId(owner.getId()).ownerName(owner.getName()).ownerEmail(owner.getEmail())
                .renterId(renter.getId()).renterName(renter.getName()).renterEmail(renter.getEmail())
                .message(req.getMessage()).status(EnquiryStatus.PENDING)
                .build();
        EnquiryDocument saved = enquiryRepository.save(enquiry);

        // Notify owner asynchronously via Kafka
        eventPublisher.publish(NotificationEvent.builder()
                .eventType(NotificationEvent.EventType.ENQUIRY_RECEIVED)
                .recipientId(owner.getId()).recipientEmail(owner.getEmail()).recipientName(owner.getName())
                .actorName(renter.getName()).propertyId(property.getId()).propertyTitle(property.getTitle())
                .extraPayload(req.getMessage()).relatedEntityId(saved.getId())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnquiryDocument> getById(@PathVariable String id, Principal principal) {
        EnquiryDocument e = enquiryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enquiry not found: " + id));
        if (!e.getRenterId().equals(principal.getName()) && !e.getOwnerId().equals(principal.getName())) {
            throw new ForbiddenException("Access denied");
        }
        return ResponseEntity.ok(e);
    }

    @GetMapping("/my-enquiries")
    public ResponseEntity<PageResponse<EnquiryDocument>> getMyEnquiries(Principal principal, Pageable pageable) {
        Page<EnquiryDocument> page = enquiryRepository.findByRenterId(principal.getName(), pageable);
        return ResponseEntity.ok(toPageResponse(page));
    }

    @GetMapping("/received")
    public ResponseEntity<PageResponse<EnquiryDocument>> getReceivedEnquiries(Principal principal, Pageable pageable) {
        Page<EnquiryDocument> page = enquiryRepository.findByOwnerId(principal.getName(), pageable);
        return ResponseEntity.ok(toPageResponse(page));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnquiryDocument> updateStatus(@PathVariable String id,
            @RequestBody EnquiryUpdateRequest req,
            Principal principal) {
        EnquiryDocument e = enquiryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enquiry not found: " + id));
        if (!e.getOwnerId().equals(principal.getName())) {
            throw new ForbiddenException("Only the owner can update enquiry status");
        }
        EnquiryStatus newStatus = EnquiryStatus.valueOf(req.getStatus().toUpperCase());
        e.setStatus(newStatus);
        e.setRejectionReason(req.getRejectionReason());
        EnquiryDocument updated = enquiryRepository.save(e);

        // Notify renter via Kafka
        NotificationEvent.EventType eventType = newStatus == EnquiryStatus.ACCEPTED
                ? NotificationEvent.EventType.ENQUIRY_ACCEPTED
                : NotificationEvent.EventType.ENQUIRY_REJECTED;
        eventPublisher.publish(NotificationEvent.builder()
                .eventType(eventType)
                .recipientId(e.getRenterId()).recipientEmail(e.getRenterEmail()).recipientName(e.getRenterName())
                .actorName(e.getOwnerName()).propertyId(e.getPropertyId()).propertyTitle(e.getPropertyTitle())
                .extraPayload(req.getRejectionReason()).relatedEntityId(e.getId())
                .build());

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id, Principal principal) {
        EnquiryDocument e = enquiryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enquiry not found: " + id));
        if (!e.getRenterId().equals(principal.getName())) {
            throw new ForbiddenException("Only the requester can delete an enquiry");
        }
        enquiryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Enquiry deleted"));
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent()).pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(!page.isLast()).hasPrevious(page.getNumber() > 0).build();
    }
}
