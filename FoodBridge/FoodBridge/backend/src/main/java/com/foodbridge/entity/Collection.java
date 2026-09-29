package com.foodbridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "collections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private FoodRequest request;

    @NotNull(message = "Collected quantity is required")
    @Positive(message = "Collected quantity must be positive")
    @Column(name = "collected_quantity", nullable = false)
    private Integer collectedQuantity;

    @Column(name = "collection_time", nullable = false)
    private LocalDateTime collectionTime;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollectionStatus status;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    @Column(length = 500)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum CollectionStatus {
        PENDING, COMPLETED, FAILED
    }
}