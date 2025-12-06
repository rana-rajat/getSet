package com.getset.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String fullAddress;
    private String city;
    private String state;
    private String country;
    private String pincode;
}
