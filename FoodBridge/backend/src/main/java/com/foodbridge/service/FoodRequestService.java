package com.foodbridge.service;

import com.foodbridge.dto.RequestDtos;
import com.foodbridge.entity.Donation;
import com.foodbridge.entity.FoodRequest;
import com.foodbridge.entity.User;
import com.foodbridge.entity.Collection;
import com.foodbridge.exception.BadRequestException;
import com.foodbridge.exception.ExpiredDonationException;
import com.foodbridge.exception.InsufficientQuantityException;
import com.foodbridge.exception.InvalidStateTransitionException;
import com.foodbridge.exception.OrganizationNotVerifiedException;
import com.foodbridge.exception.ResourceNotFoundException;
import com.foodbridge.repository.DonationRepository;
import com.foodbridge.repository.FoodRequestRepository;
import com.foodbridge.repository.CollectionRepository;
import com.foodbridge.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodRequestService {

    private final FoodRequestRepository requestRepository;
    private final DonationRepository donationRepository;
    private final CollectionRepository collectionRepository;
    private final OrganizationService organizationService;
    private final NotificationService notificationService;

    public List<RequestDtos.FoodRequestResponse> getRequestsByReceiver(User receiver) {
        return requestRepository.findByReceiverOrderByRequestedAtDesc(receiver).stream()
                .map(RequestDtos.FoodRequestResponse::from)
                .toList();
    }

    public Page<RequestDtos.FoodRequestResponse> getRequestsByReceiverAndStatus(User receiver, List<FoodRequest.RequestStatus> statuses, Pageable pageable) {
        return requestRepository.findByReceiverAndStatusInPaged(receiver, statuses, pageable)
                .map(RequestDtos.FoodRequestResponse::from);
    }

    public List<RequestDtos.FoodRequestResponse> getRequestsByDonation(Donation donation) {
        return requestRepository.findByDonationOrderByRequestedAtDesc(donation).stream()
                .map(RequestDtos.FoodRequestResponse::from)
                .toList();
    }

    public List<RequestDtos.FoodRequestResponse> getRequestsByDonationAndStatus(Donation donation, List<FoodRequest.RequestStatus> statuses) {
        return requestRepository.findByDonationAndStatusIn(donation, statuses).stream()
                .map(RequestDtos.FoodRequestResponse::from)
                .toList();
    }

    public RequestDtos.FoodRequestResponse getRequestById(Long id) {
        FoodRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", id));
        return RequestDtos.FoodRequestResponse.from(request);
    }

    @Transactional
    public RequestDtos.FoodRequestResponse createRequest(User receiver, RequestDtos.FoodRequestCreateRequest request) {
        OrganizationService.OrganizationResponse org = organizationService.getOrganizationByUser(receiver);
        if (org.getVerificationStatus() != com.foodbridge.entity.Organization.VerificationStatus.VERIFIED) {
            throw new OrganizationNotVerifiedException("Your organization must be verified to request food");
        }

        Donation donation = donationRepository.findById(request.getDonationId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation", "id", request.getDonationId()));

        if (!donation.canBeRequested()) {
            throw new BadRequestException("This donation is not available for requests");
        }

        if (donation.isExpired()) {
            throw new ExpiredDonationException("This donation has expired");
        }

        int availableQuantity = donation.getAvailableQuantity();
        if (request.getRequestedQuantity() > availableQuantity) {
            throw new InsufficientQuantityException(
                    String.format("Requested quantity (%d) exceeds available quantity (%d)", request.getRequestedQuantity(), availableQuantity)
            );
        }

        if (requestRepository.existsByDonationAndReceiverAndStatusIn(donation, receiver, List.of(
                FoodRequest.RequestStatus.PENDING, FoodRequest.RequestStatus.APPROVED))) {
            throw new BadRequestException("You have already requested this donation");
        }

        FoodRequest foodRequest = FoodRequest.builder()
                .donation(donation)
                .receiver(receiver)
                .requestedQuantity(request.getRequestedQuantity())
                .status(FoodRequest.RequestStatus.PENDING)
                .build();

        foodRequest = requestRepository.save(foodRequest);

        donation.setStatus(Donation.DonationStatus.REQUESTED);
        donationRepository.save(donation);

        notificationService.createNotification(
                donation.getDonor(),
                "New Food Request",
                String.format("You have a new request for '%s' (%d %s)", donation.getFoodName(), request.getRequestedQuantity(), donation.getUnit()),
                com.foodbridge.entity.Notification.NotificationType.NEW_REQUEST
        );

        return RequestDtos.FoodRequestResponse.from(foodRequest);
    }

    @Transactional
    public RequestDtos.FoodRequestResponse approveRequest(Long id, User donor) {
        FoodRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", id));

        if (!request.getDonation().getDonor().getId().equals(donor.getId())) {
            throw new BadRequestException("You can only approve requests for your own donations");
        }

        if (!request.canBeApproved()) {
            throw new InvalidStateTransitionException("Request cannot be approved in its current state");
        }

        if (request.getDonation().isExpired()) {
            throw new ExpiredDonationException("Cannot approve request for expired donation");
        }

        int availableQuantity = request.getDonation().getAvailableQuantity();
        if (request.getRequestedQuantity() > availableQuantity) {
            throw new InsufficientQuantityException(
                    String.format("Requested quantity (%d) exceeds available quantity (%d)", request.getRequestedQuantity(), availableQuantity)
            );
        }

        request.setStatus(FoodRequest.RequestStatus.APPROVED);
        request.setApprovedAt(LocalDateTime.now());
        request = requestRepository.save(request);

        donationRepository.save(request.getDonation());

        notificationService.createNotification(
                request.getReceiver(),
                "Request Approved",
                String.format("Your request for '%s' has been approved", request.getDonation().getFoodName()),
                com.foodbridge.entity.Notification.NotificationType.REQUEST_APPROVED
        );

        return RequestDtos.FoodRequestResponse.from(request);
    }

    @Transactional
    public RequestDtos.FoodRequestResponse rejectRequest(Long id, User donor) {
        FoodRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", id));

        if (!request.getDonation().getDonor().getId().equals(donor.getId())) {
            throw new BadRequestException("You can only reject requests for your own donations");
        }

        if (!request.canBeRejected()) {
            throw new InvalidStateTransitionException("Request cannot be rejected in its current state");
        }

        request.setStatus(FoodRequest.RequestStatus.REJECTED);
        request.setRejectedAt(LocalDateTime.now());
        request = requestRepository.save(request);

        donationRepository.save(request.getDonation());

        notificationService.createNotification(
                request.getReceiver(),
                "Request Rejected",
                String.format("Your request for '%s' has been rejected", request.getDonation().getFoodName()),
                com.foodbridge.entity.Notification.NotificationType.REQUEST_REJECTED
        );

        return RequestDtos.FoodRequestResponse.from(request);
    }

    @Transactional
    public RequestDtos.FoodRequestResponse cancelRequest(Long id, RequestDtos.FoodRequestActionRequest actionRequest, User currentUser) {
        FoodRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", id));

        if (!request.getReceiver().getId().equals(currentUser.getId()) &&
            !request.getDonation().getDonor().getId().equals(currentUser.getId()) &&
            !currentUser.getRole().equals(User.Role.ADMIN)) {
            throw new BadRequestException("You can only cancel your own requests");
        }

        if (!request.canBeCancelled()) {
            throw new InvalidStateTransitionException("Request cannot be cancelled in its current state");
        }

        request.setStatus(FoodRequest.RequestStatus.CANCELLED);
        if (actionRequest.getCancellationReason() != null) {
            request.setCancellationReason(actionRequest.getCancellationReason());
        }
        request = requestRepository.save(request);

        donationRepository.save(request.getDonation());

        String notifyUser = request.getReceiver().getId().equals(currentUser.getId()) ?
                request.getDonation().getDonor().getName() : request.getReceiver().getName();

        notificationService.createNotification(
                request.getReceiver().getId().equals(currentUser.getId()) ? request.getDonation().getDonor() : request.getReceiver(),
                "Request Cancelled",
                String.format("Request for '%s' has been cancelled by %s", request.getDonation().getFoodName(), notifyUser),
                com.foodbridge.entity.Notification.NotificationType.REQUEST_CANCELLED
        );

        return RequestDtos.FoodRequestResponse.from(request);
    }

    @Transactional
    public RequestDtos.FoodRequestResponse collectFood(Long id, RequestDtos.CollectionCreateRequest collectRequest, User donor) {
        FoodRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", id));

        if (!request.getDonation().getDonor().getId().equals(donor.getId())) {
            throw new BadRequestException("You can only mark collection for your own donations");
        }

        if (!request.canBeCollected()) {
            throw new InvalidStateTransitionException("Request cannot be collected in its current state");
        }

        if (collectionRepository.existsByRequest(request)) {
            throw new BadRequestException("Collection already recorded for this request");
        }

        if (collectRequest.getCollectedQuantity() > request.getRequestedQuantity()) {
            throw new BadRequestException("Collected quantity cannot exceed requested quantity");
        }

        Collection collection = Collection.builder()
                .request(request)
                .collectedQuantity(collectRequest.getCollectedQuantity())
                .collectionTime(collectRequest.getCollectionTime() != null ? collectRequest.getCollectionTime() : LocalDateTime.now())
                .status(com.foodbridge.entity.Collection.CollectionStatus.COMPLETED)
                .remarks(collectRequest.getRemarks())
                .build();

        collectionRepository.save(collection);

        request.setStatus(FoodRequest.RequestStatus.COLLECTED);
        request = requestRepository.save(request);

        donationRepository.save(request.getDonation());

        notificationService.createNotification(
                request.getReceiver(),
                "Food Collected",
                String.format("Food for '%s' has been collected (%d %s)", request.getDonation().getFoodName(), collectRequest.getCollectedQuantity(), request.getDonation().getUnit()),
                com.foodbridge.entity.Notification.NotificationType.FOOD_COLLECTED
        );

        return RequestDtos.FoodRequestResponse.from(request);
    }

    @Transactional
    public void completeRequest(Long id) {
        FoodRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", id));

        if (request.getStatus() != FoodRequest.RequestStatus.COLLECTED) {
            throw new InvalidStateTransitionException("Only collected requests can be marked as completed");
        }

        request.setStatus(FoodRequest.RequestStatus.COMPLETED);
        requestRepository.save(request);

        donationRepository.save(request.getDonation());
    }

    public long countByReceiverAndStatus(User receiver, FoodRequest.RequestStatus status) {
        return requestRepository.countByReceiverAndStatus(receiver, status);
    }

    public long countByDonationAndStatus(Donation donation, FoodRequest.RequestStatus status) {
        return requestRepository.countByDonationAndStatus(donation, status);
    }

    public List<RequestDtos.FoodRequestResponse> getAllRequests() {
        return requestRepository.findAllOrderByRequestedAtDesc().stream()
                .map(RequestDtos.FoodRequestResponse::from)
                .toList();
    }
}