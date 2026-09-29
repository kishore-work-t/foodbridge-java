package com.foodbridge.controller;

import com.foodbridge.dto.DashboardDtos;
import com.foodbridge.entity.User;
import com.foodbridge.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDtos.AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/donor")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<DashboardDtos.DonorDashboardResponse> getDonorDashboard(@AuthenticationPrincipal User donor) {
        return ResponseEntity.ok(dashboardService.getDonorDashboard(donor));
    }

    @GetMapping("/receiver")
    @PreAuthorize("hasRole('RECEIVER')")
    public ResponseEntity<DashboardDtos.ReceiverDashboardResponse> getReceiverDashboard(@AuthenticationPrincipal User receiver) {
        return ResponseEntity.ok(dashboardService.getReceiverDashboard(receiver));
    }
}