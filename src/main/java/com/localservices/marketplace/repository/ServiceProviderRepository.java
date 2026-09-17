package com.localservices.marketplace.repository;

import com.localservices.marketplace.model.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Integer> {

    List<ServiceProvider> findByCategory(String category);

    List<ServiceProvider> findByLocation(String location);
}