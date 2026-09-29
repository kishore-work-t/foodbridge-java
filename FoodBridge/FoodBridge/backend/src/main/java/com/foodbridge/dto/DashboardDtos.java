package com.foodbridge.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private Long totalUsers;
    private Long totalDonors;
    private Long totalReceivers;
    private Long verifiedOrganizations;
    private Long pendingOrganizations;
    private Long totalDonations;
    private Long activeDonations;
    private Long completedDonations;
    private Long expiredDonations;
    private Long pendingRequests;
    private Long completedCollections;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorDashboardResponse {

    private Long totalDonations;
    private Long activeDonations;
    private Long pendingRequests;
    private Long approvedRequests;
    private Long completedDonations;
    private Long expiredDonations;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiverDashboardResponse {

    private Long availableDonations;
    private Long myRequests;
    private Long pendingRequests;
    private Long approvedRequests;
    private Long completedCollections;
}