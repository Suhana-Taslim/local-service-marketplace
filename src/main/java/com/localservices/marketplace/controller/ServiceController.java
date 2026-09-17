package com.localservices.marketplace.controller;

import com.localservices.marketplace.model.Service;
import com.localservices.marketplace.service.ServiceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<Service> getAllServices() {
        return serviceService.getAllServices();
    }

    @PostMapping
    public Service addService(@RequestBody Service service) {
        return serviceService.addService(service);
    }
}