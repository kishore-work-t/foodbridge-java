package com.foodbridge.controller;

import com.foodbridge.dto.DonationDtos;
import com.foodbridge.entity.Donation;
import com.foodbridge.entity.User;
import com.foodbridge.service.DonationService;
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
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @GetMapping
    public ResponseEntity<Page<DonationDtos.DonationSummaryResponse>> getAvailableDonations(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(donationService.getAvailableDonationsPaged(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<DonationDtos.DonationSummaryResponse>> getFilteredDonations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String foodType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "expiry") String sortBy) {
        return ResponseEntity.ok(donationService.getFilteredDonations(search, foodType, city, sortBy));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DonationDtos.DonationResponse>> getAllDonationsForAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Donation.DonationStatus status) {
        return ResponseEntity.ok(donationService.getAllDonationsForAdmin(search, status));
    }

    @GetMapping("/my-donations")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<List<DonationDtos.DonationResponse>> getMyDonations(@AuthenticationPrincipal User donor) {
        return ResponseEntity.ok(donationService.getDonationsByDonor(donor));
    }

    @GetMapping("/my-donations/status")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<List<DonationDtos.DonationResponse>> getMyDonationsByStatus(
            @AuthenticationPrincipal User donor,
            @RequestParam List<Donation.DonationStatus> statuses) {
        return ResponseEntity.ok(donationService.getDonationsByDonorAndStatus(donor, statuses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationDtos.DonationResponse> getDonationById(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.getDonationById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<DonationDtos.DonationResponse> createDonation(
            @AuthenticationPrincipal User donor,
            @Valid @RequestBody DonationDtos.DonationCreateRequest request) {
        return ResponseEntity.status(201).body(donationService.createDonation(donor, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<DonationDtos.DonationResponse> updateDonation(
            @PathVariable Long id,
            @Valid @RequestBody DonationDtos.DonationUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(donationService.updateDonation(id, request, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<Void> cancelDonation(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        donationService.cancelDonation(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<Void> deleteDonation(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        donationService.cancelDonation(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}