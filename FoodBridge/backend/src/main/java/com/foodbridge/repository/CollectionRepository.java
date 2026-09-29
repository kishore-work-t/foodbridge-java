package com.foodbridge.repository;

import com.foodbridge.entity.Collection;
import com.foodbridge.entity.FoodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> findByRequest(FoodRequest request);

    boolean existsByRequest(FoodRequest request);
}