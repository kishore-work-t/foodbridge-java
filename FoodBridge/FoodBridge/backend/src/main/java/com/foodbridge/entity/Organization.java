package com.foodbridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Organization name is required")
    @Size(max = 200, message = "Organization name must not exceed 200 characters")
    @Column(name = "organization_name", nullable = false, length = 200)
    private String organizationName;

    @Size(max = 100, message = "Registration number must not exceed 100 characters")
    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(length = 500)
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(length = 100)
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    @Column(length = 100)
    private String state;

    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @NotNull(message = "Verification status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }
}