package com.localservices.marketplace.repository;

import com.localservices.marketplace.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
}