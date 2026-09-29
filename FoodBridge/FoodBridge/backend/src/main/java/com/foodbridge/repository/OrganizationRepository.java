package com.foodbridge.repository;

import com.foodbridge.entity.Organization;
import com.foodbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByUser(User user);

    Optional<Organization> findByUserId(Long userId);

    List<Organization> findByVerificationStatus(Organization.VerificationStatus status);

    @Query("SELECT o FROM Organization o WHERE o.verificationStatus = :status ORDER BY o.createdAt DESC")
    List<Organization> findByVerificationStatusOrderByCreatedAtDesc(@Param("status") Organization.VerificationStatus status);

    long countByVerificationStatus(Organization.VerificationStatus status);

    @Query("SELECT o FROM Organization o WHERE o.organizationName LIKE %:search% ORDER BY o.createdAt DESC")
    List<Organization> searchByName(@Param("search") String search);
}