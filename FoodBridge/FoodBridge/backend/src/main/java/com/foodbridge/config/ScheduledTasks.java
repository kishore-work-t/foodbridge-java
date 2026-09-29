package com.foodbridge.config;

import com.foodbridge.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTasks {

    private final DonationService donationService;

    @Scheduled(fixedRate = 300000)
    public void updateExpiredDonations() {
        donationService.updateExpiredDonations();
    }
}