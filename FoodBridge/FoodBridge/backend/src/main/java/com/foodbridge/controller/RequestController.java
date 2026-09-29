package com.foodbridge.controller;

import com.foodbridge.dto.RequestDtos;
import com.foodbridge.entity.Donation;
import com.foodbridge.entity.FoodRequest;
import com.foodbridge.entity.User;
import com.foodbridge.service.DonationService;
import com.foodbridge.service.FoodRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final FoodRequestService requestService;
    private final DonationService donationService;

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('RECEIVER')")
    public ResponseEntity<List<RequestDtos.FoodRequestResponse>> getMyRequests(@AuthenticationPrincipal User receiver) {
        return ResponseEntity.ok(requestService.getRequestsByReceiver(receiver));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RequestDtos.FoodRequestResponse>> getAllRequests(
            @RequestParam(required = false) FoodRequest.RequestStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        List<RequestDtos.FoodRequestResponse> requests;
        if (status != null) {
            requests = requestService.getAllRequests().stream()
                    .filter(r -> r.getStatus() == status)
                    .toList();
        } else {
            requests = requestService.getAllRequests();
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), requests.size());
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(
                requests.subList(start, end), pageable, requests.size()));
    }

    @GetMapping("/my-requests/status")
    @PreAuthorize("hasRole('RECEIVER')")
    public ResponseEntity<Page<RequestDtos.FoodRequestResponse>> getMyRequestsByStatus(
            @AuthenticationPrincipal User receiver,
            @RequestParam List<FoodRequest.RequestStatus> statuses,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(requestService.getRequestsByReceiverAndStatus(receiver, statuses, pageable));
    }

    @GetMapping("/donation/{donationId}")
    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    public ResponseEntity<List<RequestDtos.FoodRequestResponse>> getRequestsForDonation(
            @PathVariable Long donationId,
            @AuthenticationPrincipal User currentUser) {
        Donation donation = donationService.getDonationEntityById(donationId);
        if (!donation.getDonor().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(requestService.getRequestsByDonation(donation));
    }

    @GetMapping("/donation/{donationId}/status")
    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    public ResponseEntity<List<RequestDtos.FoodRequestResponse>> getRequestsForDonationByStatus(
            @PathVariable Long donationId,
            @RequestParam List<FoodRequest.RequestStatus> statuses,
            @AuthenticationPrincipal User currentUser) {
        Donation donation = donationService.getDonationEntityById(donationId);
        if (!donation.getDonor().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(requestService.getRequestsByDonationAndStatus(donation, statuses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestDtos.FoodRequestResponse> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('RECEIVER')")
    public ResponseEntity<RequestDtos.FoodRequestResponse> createRequest(
            @AuthenticationPrincipal User receiver,
            @Valid @RequestBody RequestDtos.FoodRequestCreateRequest request) {
        return ResponseEntity.status(201).body(requestService.createRequest(receiver, request));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<RequestDtos.FoodRequestResponse> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User donor) {
        return ResponseEntity.ok(requestService.approveRequest(id, donor));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<RequestDtos.FoodRequestResponse> rejectRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User donor) {
        return ResponseEntity.ok(requestService.rejectRequest(id, donor));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('DONOR') or hasRole('RECEIVER') or hasRole('ADMIN')")
    public ResponseEntity<RequestDtos.FoodRequestResponse> cancelRequest(
            @PathVariable Long id,
            @Valid @RequestBody RequestDtos.FoodRequestActionRequest actionRequest,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(requestService.cancelRequest(id, actionRequest, currentUser));
    }

    @PatchMapping("/{id}/collect")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<RequestDtos.FoodRequestResponse> collectFood(
            @PathVariable Long id,
            @Valid @RequestBody RequestDtos.CollectionCreateRequest collectRequest,
            @AuthenticationPrincipal User donor) {
        return ResponseEntity.ok(requestService.collectFood(id, collectRequest, donor));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> completeRequest(@PathVariable Long id) {
        requestService.completeRequest(id);
        return ResponseEntity.ok().build();
    }
}