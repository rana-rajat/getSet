package com.getset.enquiry.dto;

import lombok.Data;

@Data
public class EnquiryUpdateRequest {
    private String status; // ACCEPTED or REJECTED
    private String rejectionReason;
}
