package com.localservices.marketplace.controller;

import com.localservices.marketplace.model.ServiceProvider;
import com.localservices.marketplace.service.ServiceProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
@CrossOrigin
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;

    public ServiceProviderController(ServiceProviderService serviceProviderService) {
        this.serviceProviderService = serviceProviderService;
    }

    @GetMapping
    public List<ServiceProvider> getAllProviders() {
        return serviceProviderService.getAllProviders();
    }

    @PostMapping
    public ResponseEntity<ServiceProvider> addProvider(
            @RequestBody ServiceProvider provider) {

        return ResponseEntity.ok(
                serviceProviderService.addProvider(provider)
        );
    }

    @GetMapping("/category/{category}")
    public List<ServiceProvider> getByCategory(
            @PathVariable String category) {

        return serviceProviderService.getProvidersByCategory(category);
    }

    @GetMapping("/location/{location}")
    public List<ServiceProvider> getByLocation(
            @PathVariable String location) {

        return serviceProviderService.getProvidersByLocation(location);
    }
}