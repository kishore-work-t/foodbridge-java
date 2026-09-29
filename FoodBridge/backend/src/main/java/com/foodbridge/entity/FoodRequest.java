package com.foodbridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @NotNull(message = "Requested quantity is required")
    @Positive(message = "Requested quantity must be positive")
    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection collection;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED, CANCELLED, COLLECTED, COMPLETED
    }

    public boolean canBeApproved() {
        return status == RequestStatus.PENDING && donation != null && donation.canBeRequested();
    }

    public boolean canBeRejected() {
        return status == RequestStatus.PENDING;
    }

    public boolean canBeCancelled() {
        return status == RequestStatus.PENDING || status == RequestStatus.APPROVED;
    }

    public boolean canBeCollected() {
        return status == RequestStatus.APPROVED;
    }
}