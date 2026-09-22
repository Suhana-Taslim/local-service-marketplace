package com.localservices.marketplace.repository;

import com.localservices.marketplace.model.EmergencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyRequestRepository
        extends JpaRepository<EmergencyRequest, Integer> {

    List<EmergencyRequest> findByCustomerId(Integer customerId);

    List<EmergencyRequest> findByMatchedProviderId(Integer providerId);

    List<EmergencyRequest> findByStatus(String status);
}