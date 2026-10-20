package com.foodbridge.repository;

import com.foodbridge.entity.Donation;
import com.foodbridge.entity.FoodRequest;
import com.foodbridge.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRequestRepository extends JpaRepository<FoodRequest, Long> {

    List<FoodRequest> findByReceiver(User receiver);

    List<FoodRequest> findByReceiverOrderByRequestedAtDesc(User receiver);

    List<FoodRequest> findByDonation(Donation donation);

    List<FoodRequest> findByDonationOrderByRequestedAtDesc(Donation donation);

    List<FoodRequest> findByStatus(FoodRequest.RequestStatus status);

    @Query("SELECT r FROM FoodRequest r WHERE r.donation = :donation AND r.status IN :statuses")
    List<FoodRequest> findByDonationAndStatusIn(@Param("donation") Donation donation, @Param("statuses") List<FoodRequest.RequestStatus> statuses);

    @Query("SELECT COALESCE(SUM(r.requestedQuantity), 0) FROM FoodRequest r WHERE r.donation = :donation AND r.status IN :statuses")
    Integer getTotalApprovedQuantity(@Param("donation") Donation donation, @Param("statuses") List<FoodRequest.RequestStatus> statuses);

    @Query("SELECT r FROM FoodRequest r WHERE r.receiver = :receiver AND r.status IN :statuses ORDER BY r.requestedAt DESC")
    List<FoodRequest> findByReceiverAndStatusIn(@Param("receiver") User receiver, @Param("statuses") List<FoodRequest.RequestStatus> statuses);

    @Query("SELECT r FROM FoodRequest r WHERE r.receiver = :receiver AND r.status IN :statuses ORDER BY r.requestedAt DESC")
    Page<FoodRequest> findByReceiverAndStatusInPaged(@Param("receiver") User receiver, @Param("statuses") List<FoodRequest.RequestStatus> statuses, Pageable pageable);

    long countByStatus(FoodRequest.RequestStatus status);

    long countByReceiver(User receiver);

    long countByReceiverAndStatus(User receiver, FoodRequest.RequestStatus status);

    @Query("SELECT COUNT(r) FROM FoodRequest r WHERE r.donation.donor = :donor AND r.status IN :statuses")
    long countByDonationAndStatusIn(@Param("donor") User donor, @Param("statuses") List<FoodRequest.RequestStatus> statuses);

    long countByDonationAndStatus(Donation donation, FoodRequest.RequestStatus status);

    boolean existsByDonationAndReceiverAndStatusIn(Donation donation, User receiver, List<FoodRequest.RequestStatus> statuses);

    @Query("SELECT r FROM FoodRequest r ORDER BY r.requestedAt DESC")
    List<FoodRequest> findAllOrderByRequestedAtDesc();
}