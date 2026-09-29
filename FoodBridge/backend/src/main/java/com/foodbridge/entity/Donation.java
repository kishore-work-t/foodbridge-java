package com.foodbridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "donations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @NotBlank(message = "Food name is required")
    @Size(max = 200, message = "Food name must not exceed 200 characters")
    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotBlank(message = "Food type is required")
    @Size(max = 50, message = "Food type must not exceed 50 characters")
    @Column(name = "food_type", nullable = false, length = 50)
    private String foodType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;

    @NotBlank(message = "Unit is required")
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    @Column(nullable = false, length = 20)
    private String unit;

    @NotNull(message = "Preparation time is required")
    @Column(name = "preparation_time", nullable = false)
    private LocalDateTime preparationTime;

    @NotNull(message = "Expiry time is required")
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @NotBlank(message = "Pickup address is required")
    @Size(max = 500, message = "Pickup address must not exceed 500 characters")
    @Column(name = "pickup_address", nullable = false, length = 500)
    private String pickupAddress;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DonationStatus status;

    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "donation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DonationItem> items;

    @OneToMany(mappedBy = "donation", fetch = FetchType.LAZY)
    private List<FoodRequest> requests;

    public enum DonationStatus {
        AVAILABLE, REQUESTED, PARTIALLY_RESERVED, FULLY_RESERVED, EXPIRED, CANCELLED, COMPLETED
    }

    public Integer getAvailableQuantity() {
        return quantity - (reservedQuantity != null ? reservedQuantity : 0);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean canBeRequested() {
        return status == DonationStatus.AVAILABLE || status == DonationStatus.REQUESTED || status == DonationStatus.PARTIALLY_RESERVED;
    }
}