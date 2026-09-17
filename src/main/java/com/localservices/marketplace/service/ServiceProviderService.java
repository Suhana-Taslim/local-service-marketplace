package com.localservices.marketplace.service;

import com.localservices.marketplace.model.ServiceProvider;
import com.localservices.marketplace.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;

    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public List<ServiceProvider> getAllProviders() {
        return serviceProviderRepository.findAll();
    }

    public ServiceProvider addProvider(ServiceProvider provider) {
        return serviceProviderRepository.save(provider);
    }

    public List<ServiceProvider> getProvidersByCategory(String category) {
        return serviceProviderRepository.findByCategory(category);
    }

    public List<ServiceProvider> getProvidersByLocation(String location) {
        return serviceProviderRepository.findByLocation(location);
    }
}