package com.foodbridge.service;

import com.foodbridge.dto.OrganizationDtos;
import com.foodbridge.entity.Organization;
import com.foodbridge.entity.User;
import com.foodbridge.exception.BadRequestException;
import com.foodbridge.exception.ResourceNotFoundException;
import com.foodbridge.repository.OrganizationRepository;
import com.foodbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public List<OrganizationDtos.OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(OrganizationDtos.OrganizationResponse::from)
                .toList();
    }

    public Page<OrganizationDtos.OrganizationResponse> getAllOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(OrganizationDtos.OrganizationResponse::from);
    }

    public Page<OrganizationDtos.OrganizationResponse> getAllOrganizations(String search, Organization.VerificationStatus status, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return organizationRepository.searchByName(search).stream()
                    .filter(o -> status == null || o.getVerificationStatus() == status)
                    .map(OrganizationDtos.OrganizationResponse::from)
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
        }
        if (status != null) {
            return organizationRepository.findByVerificationStatusOrderByCreatedAtDesc(status).stream()
                    .map(OrganizationDtos.OrganizationResponse::from)
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
        }
        return organizationRepository.findAll(pageable).map(OrganizationDtos.OrganizationResponse::from);
    }

    public List<OrganizationDtos.OrganizationResponse> getOrganizationsByStatus(Organization.VerificationStatus status) {
        return organizationRepository.findByVerificationStatusOrderByCreatedAtDesc(status).stream()
                .map(OrganizationDtos.OrganizationResponse::from)
                .toList();
    }

    public OrganizationDtos.OrganizationResponse getOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return OrganizationDtos.OrganizationResponse.from(organization);
    }

    public OrganizationDtos.OrganizationResponse getOrganizationByUser(User user) {
        Organization organization = organizationRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "userId", user.getId()));
        return OrganizationDtos.OrganizationResponse.from(organization);
    }

    @Transactional
    public OrganizationDtos.OrganizationResponse createOrganization(User user, OrganizationDtos.OrganizationCreateRequest request) {
        if (organizationRepository.findByUser(user).isPresent()) {
            throw new BadRequestException("Organization already exists for this user");
        }

        Organization organization = Organization.builder()
                .user(user)
                .organizationName(request.getOrganizationName())
                .registrationNumber(request.getRegistrationNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .contactNumber(request.getContactNumber())
                .verificationStatus(Organization.VerificationStatus.PENDING)
                .build();

        organization = organizationRepository.save(organization);
        return OrganizationDtos.OrganizationResponse.from(organization);
    }

    @Transactional
    public OrganizationDtos.OrganizationResponse updateOrganization(Long id, OrganizationDtos.OrganizationUpdateRequest request, User currentUser) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !organization.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only update your own organization");
        }

        if (request.getOrganizationName() != null) {
            organization.setOrganizationName(request.getOrganizationName());
        }
        if (request.getRegistrationNumber() != null) {
            organization.setRegistrationNumber(request.getRegistrationNumber());
        }
        if (request.getAddress() != null) {
            organization.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            organization.setCity(request.getCity());
        }
        if (request.getState() != null) {
            organization.setState(request.getState());
        }
        if (request.getContactNumber() != null) {
            organization.setContactNumber(request.getContactNumber());
        }

        organization = organizationRepository.save(organization);
        return OrganizationDtos.OrganizationResponse.from(organization);
    }

    @Transactional
    public OrganizationDtos.OrganizationResponse verifyOrganization(Long id, OrganizationDtos.OrganizationVerificationRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        organization.setVerificationStatus(request.getVerificationStatus());
        organization = organizationRepository.save(organization);
        return OrganizationDtos.OrganizationResponse.from(organization);
    }

    public long countByVerificationStatus(Organization.VerificationStatus status) {
        return organizationRepository.countByVerificationStatus(status);
    }
}