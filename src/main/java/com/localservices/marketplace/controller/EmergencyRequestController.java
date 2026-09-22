package com.localservices.marketplace.controller;

import com.localservices.marketplace.model.EmergencyRequest;
import com.localservices.marketplace.service.EmergencyRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency")
@CrossOrigin
public class EmergencyRequestController {

    private final EmergencyRequestService emergencyService;

    public EmergencyRequestController(
            EmergencyRequestService emergencyService) {
        this.emergencyService = emergencyService;
    }

    @PostMapping
    public ResponseEntity<EmergencyRequest> createRequest(
            @RequestBody EmergencyRequest request) {

        return ResponseEntity.ok(
                emergencyService.createEmergencyRequest(request)
        );
    }

    @GetMapping("/customer/{customerId}")
    public List<EmergencyRequest> getCustomerRequests(
            @PathVariable Integer customerId) {

        return emergencyService.getCustomerRequests(customerId);
    }

    @GetMapping("/provider/{providerId}")
    public List<EmergencyRequest> getProviderRequests(
            @PathVariable Integer providerId) {

        return emergencyService.getProviderRequests(providerId);
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<EmergencyRequest> updateStatus(
            @PathVariable Integer requestId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                emergencyService.updateStatus(
                        requestId,
                        status
                )
        );
    }
}