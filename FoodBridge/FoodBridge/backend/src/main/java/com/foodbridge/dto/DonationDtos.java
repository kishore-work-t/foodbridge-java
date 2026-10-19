package com.foodbridge.dto;

import com.foodbridge.entity.Donation;
import com.foodbridge.entity.DonationItem;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class DonationDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DonationResponse {

        private Long id;
        private Long donorId;
        private String donorName;
        private String foodName;
        private String description;
        private String foodType;
        private Integer quantity;
        private String unit;
        private LocalDateTime preparationTime;
        private LocalDateTime expiryTime;
        private String pickupAddress;
        private Donation.DonationStatus status;
        private Integer reservedQuantity;
        private Integer availableQuantity;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<DonationItemResponse> items;

        public static DonationResponse from(Donation donation) {
            DonationResponse response = DonationResponse.builder()
                    .id(donation.getId())
                    .donorId(donation.getDonor() != null ? donation.getDonor().getId() : null)
                    .donorName(donation.getDonor() != null ? donation.getDonor().getName() : null)
                    .foodName(donation.getFoodName())
                    .description(donation.getDescription())
                    .foodType(donation.getFoodType())
                    .quantity(donation.getQuantity())
                    .unit(donation.getUnit())
                    .preparationTime(donation.getPreparationTime())
                    .expiryTime(donation.getExpiryTime())
                    .pickupAddress(donation.getPickupAddress())
                    .status(donation.getStatus())
                    .reservedQuantity(donation.getReservedQuantity())
                    .availableQuantity(donation.getAvailableQuantity())
                    .createdAt(donation.getCreatedAt())
                    .updatedAt(donation.getUpdatedAt())
                    .build();
            if (donation.getItems() != null) {
                response.setItems(donation.getItems().stream()
                        .map(DonationItemResponse::from)
                        .toList());
            }
            return response;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DonationCreateRequest {

        @NotBlank(message = "Food name is required")
        @Size(max = 200, message = "Food name must not exceed 200 characters")
        private String foodName;

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;

        @NotBlank(message = "Food type is required")
        @Size(max = 50, message = "Food type must not exceed 50 characters")
        private String foodType;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotBlank(message = "Unit is required")
        @Size(max = 20, message = "Unit must not exceed 20 characters")
        private String unit;

        @NotNull(message = "Preparation time is required")
        private LocalDateTime preparationTime;

        @NotNull(message = "Expiry time is required")
        private LocalDateTime expiryTime;

        @NotBlank(message = "Pickup address is required")
        @Size(max = 500, message = "Pickup address must not exceed 500 characters")
        private String pickupAddress;

        private List<DonationItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DonationUpdateRequest {

        @Size(max = 200, message = "Food name must not exceed 200 characters")
        private String foodName;

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;

        @Size(max = 50, message = "Food type must not exceed 50 characters")
        private String foodType;

        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @Size(max = 20, message = "Unit must not exceed 20 characters")
        private String unit;

        private LocalDateTime preparationTime;

        private LocalDateTime expiryTime;

        @Size(max = 500, message = "Pickup address must not exceed 500 characters")
        private String pickupAddress;

        private List<DonationItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DonationItemRequest {

        @NotBlank(message = "Item name is required")
        @Size(max = 200, message = "Item name must not exceed 200 characters")
        private String itemName;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotBlank(message = "Unit is required")
        @Size(max = 20, message = "Unit must not exceed 20 characters")
        private String unit;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DonationItemResponse {

        private Long id;
        private String itemName;
        private Integer quantity;
        private String unit;
        private String description;
        private LocalDateTime createdAt;

        public static DonationItemResponse from(DonationItem item) {
            return DonationItemResponse.builder()
                    .id(item.getId())
                    .itemName(item.getItemName())
                    .quantity(item.getQuantity())
                    .unit(item.getUnit())
                    .description(item.getDescription())
                    .createdAt(item.getCreatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DonationSummaryResponse {

        private Long id;
        private String foodName;
        private String foodType;
        private Integer quantity;
        private String unit;
        private LocalDateTime expiryTime;
        private String pickupAddress;
        private Donation.DonationStatus status;
        private Integer availableQuantity;
        private String donorName;
    }
}