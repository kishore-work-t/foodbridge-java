package com.foodbridge.repository;

import com.foodbridge.entity.Donation;
import com.foodbridge.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonor(User donor);

    List<Donation> findByDonorOrderByCreatedAtDesc(User donor);

    List<Donation> findByStatus(Donation.DonationStatus status);

    @Query("SELECT d FROM Donation d WHERE d.status IN :statuses ORDER BY d.expiryTime ASC")
    List<Donation> findByStatusInOrderByExpiryTimeAsc(@Param("statuses") List<Donation.DonationStatus> statuses);

    @Query("SELECT d FROM Donation d WHERE d.status IN :statuses AND d.expiryTime > :now ORDER BY d.expiryTime ASC")
    List<Donation> findAvailableDonations(@Param("statuses") List<Donation.DonationStatus> statuses, @Param("now") LocalDateTime now);

    @Query("SELECT d FROM Donation d WHERE d.status IN :statuses AND d.expiryTime > :now ORDER BY d.createdAt DESC")
    Page<Donation> findAvailableDonationsPaged(@Param("statuses") List<Donation.DonationStatus> statuses, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT d FROM Donation d WHERE d.donor = :donor AND d.status IN :statuses ORDER BY d.createdAt DESC")
    List<Donation> findByDonorAndStatusIn(@Param("donor") User donor, @Param("statuses") List<Donation.DonationStatus> statuses);

    @Query("SELECT d FROM Donation d WHERE d.expiryTime < :now AND d.status NOT IN :excludedStatuses")
    List<Donation> findExpiredDonations(@Param("now") LocalDateTime now, @Param("excludedStatuses") List<Donation.DonationStatus> excludedStatuses);

    long countByDonor(User donor);

    long countByDonorAndStatus(User donor, Donation.DonationStatus status);

    @Query("SELECT COUNT(d) FROM Donation d WHERE d.donor = :donor AND d.status IN :statuses")
    long countByDonorAndStatusIn(@Param("donor") User donor, @Param("statuses") List<Donation.DonationStatus> statuses);

    long countByStatus(Donation.DonationStatus status);

    @Query("SELECT COUNT(d) FROM Donation d WHERE d.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<Donation.DonationStatus> statuses);
}