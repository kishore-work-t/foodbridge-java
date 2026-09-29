package com.foodbridge.repository;

import com.foodbridge.entity.Donation;
import com.foodbridge.entity.DonationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationItemRepository extends JpaRepository<DonationItem, Long> {

    List<DonationItem> findByDonation(Donation donation);

    void deleteByDonation(Donation donation);
}