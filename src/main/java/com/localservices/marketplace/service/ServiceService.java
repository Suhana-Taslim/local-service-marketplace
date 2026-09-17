package com.localservices.marketplace.service;

import com.localservices.marketplace.model.Service;
import com.localservices.marketplace.repository.ServiceRepository;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<com.localservices.marketplace.model.Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public com.localservices.marketplace.model.Service addService(
            com.localservices.marketplace.model.Service service) {
        return serviceRepository.save(service);
    }
}