package com.foodbridge.service;

import com.foodbridge.dto.DonationDtos;
import com.foodbridge.entity.Donation;
import com.foodbridge.entity.DonationItem;
import com.foodbridge.entity.User;
import com.foodbridge.exception.BadRequestException;
import com.foodbridge.exception.ResourceNotFoundException;
import com.foodbridge.repository.DonationRepository;
import com.foodbridge.repository.DonationItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonationItemRepository donationItemRepository;

    public List<DonationDtos.DonationSummaryResponse> getAvailableDonations() {
        List<Donation.DonationStatus> availableStatuses = List.of(
                Donation.DonationStatus.AVAILABLE,
                Donation.DonationStatus.REQUESTED,
                Donation.DonationStatus.PARTIALLY_RESERVED
        );
        LocalDateTime now = LocalDateTime.now();
        return donationRepository.findAvailableDonations(availableStatuses, now).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public Page<DonationDtos.DonationSummaryResponse> getAvailableDonationsPaged(Pageable pageable) {
        List<Donation.DonationStatus> availableStatuses = List.of(
                Donation.DonationStatus.AVAILABLE,
                Donation.DonationStatus.REQUESTED,
                Donation.DonationStatus.PARTIALLY_RESERVED
        );
        LocalDateTime now = LocalDateTime.now();
        return donationRepository.findAvailableDonationsPaged(availableStatuses, now, pageable)
                .map(this::toSummaryResponse);
    }

    public List<DonationDtos.DonationResponse> getDonationsByDonor(User donor) {
        return donationRepository.findByDonorOrderByCreatedAtDesc(donor).stream()
                .map(DonationDtos.DonationResponse::from)
                .toList();
    }

    public List<DonationDtos.DonationResponse> getDonationsByDonorAndStatus(User donor, List<Donation.DonationStatus> statuses) {
        return donationRepository.findByDonorAndStatusIn(donor, statuses).stream()
                .map(DonationDtos.DonationResponse::from)
                .toList();
    }

    public DonationDtos.DonationResponse getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation", "id", id));
        return DonationDtos.DonationResponse.from(donation);
    }

    @Transactional
    public DonationDtos.DonationResponse createDonation(User donor, DonationDtos.DonationCreateRequest request) {
        if (request.getExpiryTime().isBefore(request.getPreparationTime())) {
            throw new BadRequestException("Expiry time must be after preparation time");
        }
        if (request.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Expiry time must be in the future");
        }

        Donation donation = Donation.builder()
                .donor(donor)
                .foodName(request.getFoodName())
                .description(request.getDescription())
                .foodType(request.getFoodType())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .preparationTime(request.getPreparationTime())
                .expiryTime(request.getExpiryTime())
                .pickupAddress(request.getPickupAddress())
                .status(Donation.DonationStatus.AVAILABLE)
                .reservedQuantity(0)
                .build();

        donation = donationRepository.save(donation);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<DonationItem> items = request.getItems().stream()
                    .map(itemReq -> DonationItem.builder()
                            .donation(donation)
                            .itemName(itemReq.getItemName())
                            .quantity(itemReq.getQuantity())
                            .unit(itemReq.getUnit())
                            .description(itemReq.getDescription())
                            .build())
                    .toList();
            donationItemRepository.saveAll(items);
            donation.setItems(items);
        }

        return DonationDtos.DonationResponse.from(donation);
    }

    @Transactional
    public DonationDtos.DonationResponse updateDonation(Long id, DonationDtos.DonationUpdateRequest request, User currentUser) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation", "id", id));

        if (!donation.getDonor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only update your own donations");
        }

        if (donation.getStatus() != Donation.DonationStatus.AVAILABLE) {
            throw new BadRequestException("Can only update donations that are still available");
        }

        if (request.getExpiryTime() != null && request.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Expiry time must be in the future");
        }

        if (request.getFoodName() != null) {
            donation.setFoodName(request.getFoodName());
        }
        if (request.getDescription() != null) {
            donation.setDescription(request.getDescription());
        }
        if (request.getFoodType() != null) {
            donation.setFoodType(request.getFoodType());
        }
        if (request.getQuantity() != null) {
            donation.setQuantity(request.getQuantity());
        }
        if (request.getUnit() != null) {
            donation.setUnit(request.getUnit());
        }
        if (request.getPreparationTime() != null) {
            donation.setPreparationTime(request.getPreparationTime());
        }
        if (request.getExpiryTime() != null) {
            donation.setExpiryTime(request.getExpiryTime());
        }
        if (request.getPickupAddress() != null) {
            donation.setPickupAddress(request.getPickupAddress());
        }

        if (request.getItems() != null) {
            donationItemRepository.deleteByDonation(donation);
            List<DonationItem> items = request.getItems().stream()
                    .map(itemReq -> DonationItem.builder()
                            .donation(donation)
                            .itemName(itemReq.getItemName())
                            .quantity(itemReq.getQuantity())
                            .unit(itemReq.getUnit())
                            .description(itemReq.getDescription())
                            .build())
                    .toList();
            donationItemRepository.saveAll(items);
            donation.setItems(items);
        }

        donation = donationRepository.save(donation);
        return DonationDtos.DonationResponse.from(donation);
    }

    @Transactional
    public void cancelDonation(Long id, User currentUser) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation", "id", id));

        if (!donation.getDonor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only cancel your own donations");
        }

        if (donation.getStatus() == Donation.DonationStatus.COMPLETED ||
            donation.getStatus() == Donation.DonationStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a completed or already cancelled donation");
        }

        donation.setStatus(Donation.DonationStatus.CANCELLED);
        donationRepository.save(donation);
    }

    @Transactional
    public void updateExpiredDonations() {
        List<Donation.DonationStatus> excludedStatuses = List.of(
                Donation.DonationStatus.COMPLETED,
                Donation.DonationStatus.CANCELLED,
                Donation.DonationStatus.EXPIRED
        );
        List<Donation> expiredDonations = donationRepository.findExpiredDonations(LocalDateTime.now(), excludedStatuses);
        for (Donation donation : expiredDonations) {
            donation.setStatus(Donation.DonationStatus.EXPIRED);
        }
        donationRepository.saveAll(expiredDonations);
    }

    @Transactional
    public void reserveQuantity(Donation donation, int quantity) {
        int available = donation.getAvailableQuantity();
        if (quantity > available) {
            throw new BadRequestException("Requested quantity exceeds available quantity. Available: " + available);
        }
        donation.setReservedQuantity(donation.getReservedQuantity() + quantity);
        updateDonationStatus(donation);
        donationRepository.save(donation);
    }

    @Transactional
    public void releaseQuantity(Donation donation, int quantity) {
        donation.setReservedQuantity(Math.max(0, donation.getReservedQuantity() - quantity));
        updateDonationStatus(donation);
        donationRepository.save(donation);
    }

    private void updateDonationStatus(Donation donation) {
        int available = donation.getAvailableQuantity();
        if (available <= 0) {
            donation.setStatus(Donation.DonationStatus.FULLY_RESERVED);
        } else if (donation.getReservedQuantity() > 0) {
            donation.setStatus(Donation.DonationStatus.PARTIALLY_RESERVED);
        } else {
            donation.setStatus(Donation.DonationStatus.AVAILABLE);
        }
    }

    public long countByDonorAndStatus(User donor, Donation.DonationStatus status) {
        return donationRepository.countByDonorAndStatus(donor, status);
    }

    public long countByStatus(Donation.DonationStatus status) {
        return donationRepository.countByStatus(status);
    }

    public long countByStatusIn(List<Donation.DonationStatus> statuses) {
        return donationRepository.countByStatusIn(statuses);
    }

    public Donation getDonationEntityById(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation", "id", id));
    }

    public List<DonationDtos.DonationSummaryResponse> getFilteredDonations(String search, String foodType, String city, String sortBy) {
        List<Donation.DonationStatus> availableStatuses = List.of(
                Donation.DonationStatus.AVAILABLE,
                Donation.DonationStatus.REQUESTED,
                Donation.DonationStatus.PARTIALLY_RESERVED
        );
        LocalDateTime now = LocalDateTime.now();
        List<Donation> donations = donationRepository.findAvailableDonations(availableStatuses, now);

        return donations.stream()
                .filter(d -> search == null || search.isEmpty() || d.getFoodName().toLowerCase().contains(search.toLowerCase()))
                .filter(d -> foodType == null || foodType.isEmpty() || d.getFoodType().equalsIgnoreCase(foodType))
                .filter(d -> city == null || city.isEmpty() || (d.getPickupAddress() != null && d.getPickupAddress().toLowerCase().contains(city.toLowerCase())))
                .sorted((d1, d2) -> {
                    if ("newest".equals(sortBy)) {
                        return d2.getCreatedAt().compareTo(d1.getCreatedAt());
                    }
                    return d1.getExpiryTime().compareTo(d2.getExpiryTime());
                })
                .map(this::toSummaryResponse)
                .toList();
    }

    public List<DonationDtos.DonationResponse> getAllDonationsForAdmin(String search, Donation.DonationStatus status) {
        List<Donation> donations = donationRepository.findAll();
        return donations.stream()
                .filter(d -> search == null || search.isEmpty() || d.getFoodName().toLowerCase().contains(search.toLowerCase()))
                .filter(d -> status == null || d.getStatus() == status)
                .map(DonationDtos.DonationResponse::from)
                .toList();
    }

    private DonationDtos.DonationSummaryResponse toSummaryResponse(Donation donation) {
        return DonationDtos.DonationSummaryResponse.builder()
                .id(donation.getId())
                .foodName(donation.getFoodName())
                .foodType(donation.getFoodType())
                .quantity(donation.getQuantity())
                .unit(donation.getUnit())
                .expiryTime(donation.getExpiryTime())
                .pickupAddress(donation.getPickupAddress())
                .status(donation.getStatus())
                .availableQuantity(donation.getAvailableQuantity())
                .donorName(donation.getDonor() != null ? donation.getDonor().getName() : null)
                .build();
    }
}