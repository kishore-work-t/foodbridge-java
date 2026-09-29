package com.foodbridge.config;

import com.foodbridge.entity.Donation;
import com.foodbridge.entity.FoodRequest;
import com.foodbridge.entity.Notification;
import com.foodbridge.entity.Organization;
import com.foodbridge.entity.User;
import com.foodbridge.repository.DonationRepository;
import com.foodbridge.repository.FoodRequestRepository;
import com.foodbridge.repository.NotificationRepository;
import com.foodbridge.repository.OrganizationRepository;
import com.foodbridge.repository.UserRepository;
import com.foodbridge.service.AuthService;
import com.foodbridge.service.DonationService;
import com.foodbridge.service.FoodRequestService;
import com.foodbridge.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final DonationRepository donationRepository;
    private final FoodRequestRepository requestRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final OrganizationService organizationService;
    private final DonationService donationService;
    private final FoodRequestService requestService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        String hashedPassword = passwordEncoder.encode("password123");

        User admin = User.builder()
                .name("Admin User")
                .email("admin@foodbridge.com")
                .password(hashedPassword)
                .phone("1234567890")
                .role(User.Role.ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(admin);

        User donor1 = User.builder()
                .name("Restaurant Owner")
                .email("donor1@foodbridge.com")
                .password(hashedPassword)
                .phone("1234567891")
                .role(User.Role.DONOR)
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(donor1);

        User donor2 = User.builder()
                .name("Hotel Manager")
                .email("donor2@foodbridge.com")
                .password(hashedPassword)
                .phone("1234567892")
                .role(User.Role.DONOR)
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(donor2);

        User receiver1 = User.builder()
                .name("NGO Director")
                .email("receiver1@foodbridge.com")
                .password(hashedPassword)
                .phone("1234567893")
                .role(User.Role.RECEIVER)
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(receiver1);

        User receiver2 = User.builder()
                .name("Charity Manager")
                .email("receiver2@foodbridge.com")
                .password(hashedPassword)
                .phone("1234567894")
                .role(User.Role.RECEIVER)
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(receiver2);

        User pendingReceiver = User.builder()
                .name("New NGO")
                .email("pending@foodbridge.com")
                .password(hashedPassword)
                .phone("1234567895")
                .role(User.Role.RECEIVER)
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(pendingReceiver);

        Organization org1 = Organization.builder()
                .user(receiver1)
                .organizationName("Helping Hands NGO")
                .registrationNumber("NGO123456")
                .address("123 Main Street")
                .city("New York")
                .state("NY")
                .contactNumber("1234567893")
                .verificationStatus(Organization.VerificationStatus.VERIFIED)
                .build();
        organizationRepository.save(org1);

        Organization org2 = Organization.builder()
                .user(receiver2)
                .organizationName("Food for All Charity")
                .registrationNumber("CHR789012")
                .address("456 Oak Avenue")
                .city("Los Angeles")
                .state("CA")
                .contactNumber("1234567894")
                .verificationStatus(Organization.VerificationStatus.VERIFIED)
                .build();
        organizationRepository.save(org2);

        Organization pendingOrg = Organization.builder()
                .user(pendingReceiver)
                .organizationName("New Hope Foundation")
                .registrationNumber("NHF345678")
                .address("789 Elm Road")
                .city("Chicago")
                .state("IL")
                .contactNumber("1234567895")
                .verificationStatus(Organization.VerificationStatus.PENDING)
                .build();
        organizationRepository.save(pendingOrg);

        Donation donation1 = Donation.builder()
                .donor(donor1)
                .foodName("Chicken Biryani")
                .description("Freshly prepared chicken biryani with raita")
                .foodType("Main Course")
                .quantity(50)
                .unit("portions")
                .preparationTime(LocalDateTime.now().minusHours(2))
                .expiryTime(LocalDateTime.now().plusHours(6))
                .pickupAddress("123 Restaurant Street, New York, NY")
                .status(Donation.DonationStatus.AVAILABLE)
                .reservedQuantity(0)
                .build();
        donationRepository.save(donation1);

        Donation donation2 = Donation.builder()
                .donor(donor1)
                .foodName("Vegetable Fried Rice")
                .description("Mixed vegetable fried rice")
                .foodType("Main Course")
                .quantity(30)
                .unit("portions")
                .preparationTime(LocalDateTime.now().minusHours(1))
                .expiryTime(LocalDateTime.now().plusHours(4))
                .pickupAddress("123 Restaurant Street, New York, NY")
                .status(Donation.DonationStatus.AVAILABLE)
                .reservedQuantity(0)
                .build();
        donationRepository.save(donation2);

        Donation donation3 = Donation.builder()
                .donor(donor2)
                .foodName("Assorted Sandwiches")
                .description("Chicken, tuna, and vegetable sandwiches")
                .foodType("Snacks")
                .quantity(40)
                .unit("pieces")
                .preparationTime(LocalDateTime.now().minusHours(3))
                .expiryTime(LocalDateTime.now().plusHours(3))
                .pickupAddress("456 Hotel Avenue, Los Angeles, CA")
                .status(Donation.DonationStatus.AVAILABLE)
                .reservedQuantity(0)
                .build();
        donationRepository.save(donation3);

        Donation donation4 = Donation.builder()
                .donor(donor2)
                .foodName("Fresh Fruit Salad")
                .description("Seasonal fresh fruit salad")
                .foodType("Dessert")
                .quantity(25)
                .unit("bowls")
                .preparationTime(LocalDateTime.now().minusDays(1))
                .expiryTime(LocalDateTime.now().minusHours(1))
                .pickupAddress("456 Hotel Avenue, Los Angeles, CA")
                .status(Donation.DonationStatus.EXPIRED)
                .reservedQuantity(0)
                .build();
        donationRepository.save(donation4);

        FoodRequest request1 = FoodRequest.builder()
                .donation(donation1)
                .receiver(receiver1)
                .requestedQuantity(20)
                .status(FoodRequest.RequestStatus.PENDING)
                .build();
        requestRepository.save(request1);

        FoodRequest request2 = FoodRequest.builder()
                .donation(donation2)
                .receiver(receiver1)
                .requestedQuantity(15)
                .status(FoodRequest.RequestStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusHours(1))
                .build();
        requestRepository.save(request2);

        donation2.setStatus(Donation.DonationStatus.PARTIALLY_RESERVED);
        donation2.setReservedQuantity(15);
        donationRepository.save(donation2);

        notificationRepository.save(Notification.builder()
                .user(donor1)
                .title("New Food Request")
                .message("You have a new request for 'Chicken Biryani' (20 portions)")
                .type(Notification.NotificationType.NEW_REQUEST)
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .user(receiver1)
                .title("Request Approved")
                .message("Your request for 'Vegetable Fried Rice' has been approved")
                .type(Notification.NotificationType.REQUEST_APPROVED)
                .isRead(false)
                .build());

        System.out.println("Sample data initialized successfully!");
        System.out.println("Demo credentials (password: password123):");
        System.out.println("Admin: admin@foodbridge.com");
        System.out.println("Donor 1: donor1@foodbridge.com");
        System.out.println("Donor 2: donor2@foodbridge.com");
        System.out.println("Receiver 1 (Verified): receiver1@foodbridge.com");
        System.out.println("Receiver 2 (Verified): receiver2@foodbridge.com");
        System.out.println("Pending Receiver: pending@foodbridge.com");
    }
}