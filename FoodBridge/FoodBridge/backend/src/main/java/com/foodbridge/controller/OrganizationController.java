package com.foodbridge.controller;

import com.foodbridge.dto.OrganizationDtos;
import com.foodbridge.entity.Organization;
import com.foodbridge.entity.User;
import com.foodbridge.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrganizationDtos.OrganizationResponse>> getAllOrganizations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Organization.VerificationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(organizationService.getAllOrganizations(search, status, pageable));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrganizationDtos.OrganizationResponse>> getPendingOrganizations(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(organizationService.getAllOrganizations(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(@organizationService.getOrganizationById(#id).userId, authentication.principal)")
    public ResponseEntity<OrganizationDtos.OrganizationResponse> getOrganizationById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('RECEIVER')")
    public ResponseEntity<OrganizationDtos.OrganizationResponse> getMyOrganization(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(organizationService.getOrganizationByUser(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('RECEIVER')")
    public ResponseEntity<OrganizationDtos.OrganizationResponse> createOrganization(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrganizationDtos.OrganizationCreateRequest request) {
        return ResponseEntity.status(201).body(organizationService.createOrganization(user, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(@organizationService.getOrganizationById(#id).userId, authentication.principal)")
    public ResponseEntity<OrganizationDtos.OrganizationResponse> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationDtos.OrganizationUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(organizationService.updateOrganization(id, request, currentUser));
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationDtos.OrganizationResponse> verifyOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationDtos.OrganizationVerificationRequest request) {
        return ResponseEntity.ok(organizationService.verifyOrganization(id, request));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('RECEIVER')")
    public ResponseEntity<OrganizationDtos.OrganizationResponse> updateMyOrganization(
            @Valid @RequestBody OrganizationDtos.OrganizationUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        OrganizationDtos.OrganizationResponse org = organizationService.getOrganizationByUser(currentUser);
        return ResponseEntity.ok(organizationService.updateOrganization(org.getId(), request, currentUser));
    }
}