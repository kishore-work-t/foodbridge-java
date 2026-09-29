package com.foodbridge.service;

import com.foodbridge.dto.DashboardDtos;
import com.foodbridge.entity.Donation;
import com.foodbridge.entity.FoodRequest;
import com.foodbridge.entity.Organization;
import com.foodbridge.entity.User;
import com.foodbridge.repository.DonationRepository;
import com.foodbridge.repository.FoodRequestRepository;
import com.foodbridge.repository.OrganizationRepository;
import com.foodbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final DonationRepository donationRepository;
    private final FoodRequestRepository requestRepository;

    public DashboardDtos.AdminDashboardResponse getAdminDashboard() {
        List<Donation.DonationStatus> activeStatuses = List.of(
                Donation.DonationStatus.AVAILABLE,
                Donation.DonationStatus.REQUESTED,
                Donation.DonationStatus.PARTIALLY_RESERVED,
                Donation.DonationStatus.FULLY_RESERVED
        );

        return DashboardDtos.AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalDonors(userRepository.countByRole(User.Role.DONOR))
                .totalReceivers(userRepository.countByRole(User.Role.RECEIVER))
                .verifiedOrganizations(organizationRepository.countByVerificationStatus(Organization.VerificationStatus.VERIFIED))
                .pendingOrganizations(organizationRepository.countByVerificationStatus(Organization.VerificationStatus.PENDING))
                .totalDonations(donationRepository.count())
                .activeDonations(donationRepository.countByStatusIn(activeStatuses))
                .completedDonations(donationRepository.countByStatus(Donation.DonationStatus.COMPLETED))
                .expiredDonations(donationRepository.countByStatus(Donation.DonationStatus.EXPIRED))
                .pendingRequests(requestRepository.countByStatus(FoodRequest.RequestStatus.PENDING))
                .completedCollections(requestRepository.countByStatus(FoodRequest.RequestStatus.COMPLETED))
                .build();
    }

    public DashboardDtos.DonorDashboardResponse getDonorDashboard(User donor) {
        List<Donation.DonationStatus> activeStatuses = List.of(
                Donation.DonationStatus.AVAILABLE,
                Donation.DonationStatus.REQUESTED,
                Donation.DonationStatus.PARTIALLY_RESERVED,
                Donation.DonationStatus.FULLY_RESERVED
        );

        return DashboardDtos.DonorDashboardResponse.builder()
                .totalDonations(donationRepository.countByDonor(donor))
                .activeDonations(donationRepository.countByDonorAndStatusIn(donor, activeStatuses))
                .pendingRequests(requestRepository.countByDonationAndStatusIn(donor, List.of(FoodRequest.RequestStatus.PENDING)))
                .approvedRequests(requestRepository.countByDonationAndStatusIn(donor, List.of(FoodRequest.RequestStatus.APPROVED)))
                .completedDonations(donationRepository.countByDonorAndStatus(donor, Donation.DonationStatus.COMPLETED))
                .expiredDonations(donationRepository.countByDonorAndStatus(donor, Donation.DonationStatus.EXPIRED))
                .build();
    }

    public DashboardDtos.ReceiverDashboardResponse getReceiverDashboard(User receiver) {
        List<Donation.DonationStatus> availableStatuses = List.of(
                Donation.DonationStatus.AVAILABLE,
                Donation.DonationStatus.REQUESTED,
                Donation.DonationStatus.PARTIALLY_RESERVED
        );

        return DashboardDtos.ReceiverDashboardResponse.builder()
                .availableDonations(donationRepository.countByStatusIn(availableStatuses))
                .myRequests(requestRepository.countByReceiver(receiver))
                .pendingRequests(requestRepository.countByReceiverAndStatus(receiver, FoodRequest.RequestStatus.PENDING))
                .approvedRequests(requestRepository.countByReceiverAndStatus(receiver, FoodRequest.RequestStatus.APPROVED))
                .completedCollections(requestRepository.countByReceiverAndStatus(receiver, FoodRequest.RequestStatus.COMPLETED))
                .build();
    }
}