package com.foodbridge.dto;

import com.foodbridge.entity.Collection;
import com.foodbridge.entity.FoodRequest;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class RequestDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodRequestResponse {

        private Long id;
        private Long donationId;
        private String donationFoodName;
        private Long receiverId;
        private String receiverName;
        private String receiverEmail;
        private Integer requestedQuantity;
        private FoodRequest.RequestStatus status;
        private LocalDateTime requestedAt;
        private LocalDateTime approvedAt;
        private LocalDateTime rejectedAt;
        private String cancellationReason;
        private CollectionResponse collection;

        public static FoodRequestResponse from(FoodRequest request) {
            FoodRequestResponse response = FoodRequestResponse.builder()
                    .id(request.getId())
                    .donationId(request.getDonation() != null ? request.getDonation().getId() : null)
                    .donationFoodName(request.getDonation() != null ? request.getDonation().getFoodName() : null)
                    .receiverId(request.getReceiver() != null ? request.getReceiver().getId() : null)
                    .receiverName(request.getReceiver() != null ? request.getReceiver().getName() : null)
                    .receiverEmail(request.getReceiver() != null ? request.getReceiver().getEmail() : null)
                    .requestedQuantity(request.getRequestedQuantity())
                    .status(request.getStatus())
                    .requestedAt(request.getRequestedAt())
                    .approvedAt(request.getApprovedAt())
                    .rejectedAt(request.getRejectedAt())
                    .cancellationReason(request.getCancellationReason())
                    .build();
            if (request.getCollection() != null) {
                response.setCollection(CollectionResponse.from(request.getCollection()));
            }
            return response;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodRequestCreateRequest {

        @NotNull(message = "Donation ID is required")
        private Long donationId;

        @NotNull(message = "Requested quantity is required")
        @Positive(message = "Requested quantity must be positive")
        private Integer requestedQuantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodRequestActionRequest {

        private String cancellationReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionResponse {

        private Long id;
        private Long requestId;
        private Integer collectedQuantity;
        private LocalDateTime collectionTime;
        private Collection.CollectionStatus status;
        private String remarks;
        private LocalDateTime createdAt;

        public static CollectionResponse from(Collection collection) {
            return CollectionResponse.builder()
                    .id(collection.getId())
                    .requestId(collection.getRequest() != null ? collection.getRequest().getId() : null)
                    .collectedQuantity(collection.getCollectedQuantity())
                    .collectionTime(collection.getCollectionTime())
                    .status(collection.getStatus())
                    .remarks(collection.getRemarks())
                    .createdAt(collection.getCreatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionCreateRequest {

        @NotNull(message = "Collected quantity is required")
        @Positive(message = "Collected quantity must be positive")
        private Integer collectedQuantity;

        @NotNull(message = "Collection time is required")
        private LocalDateTime collectionTime;

        private String remarks;
    }
}