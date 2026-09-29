package com.foodbridge.dto;

import com.foodbridge.entity.Organization;
import com.foodbridge.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {

    private Long id;
    private Long userId;
    private String organizationName;
    private String registrationNumber;
    private String address;
    private String city;
    private String state;
    private String contactNumber;
    private Organization.VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
    private UserResponse user;

    public static OrganizationResponse from(Organization org) {
        OrganizationResponse response = OrganizationResponse.builder()
                .id(org.getId())
                .userId(org.getUser().getId())
                .organizationName(org.getOrganizationName())
                .registrationNumber(org.getRegistrationNumber())
                .address(org.getAddress())
                .city(org.getCity())
                .state(org.getState())
                .contactNumber(org.getContactNumber())
                .verificationStatus(org.getVerificationStatus())
                .createdAt(org.getCreatedAt())
                .build();
        if (org.getUser() != null) {
            response.setUser(UserResponse.from(org.getUser()));
        }
        return response;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationCreateRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 200, message = "Organization name must not exceed 200 characters")
    private String organizationName;

    @Size(max = 100, message = "Registration number must not exceed 100 characters")
    private String registrationNumber;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    private String contactNumber;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationUpdateRequest {

    @Size(max = 200, message = "Organization name must not exceed 200 characters")
    private String organizationName;

    @Size(max = 100, message = "Registration number must not exceed 100 characters")
    private String registrationNumber;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    private String contactNumber;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationVerificationRequest {

    @NotNull(message = "Verification status is required")
    private Organization.VerificationStatus verificationStatus;

    private String rejectionReason;
}