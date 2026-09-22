package com.localservices.marketplace.service;

import com.localservices.marketplace.model.EmergencyRequest;
import com.localservices.marketplace.model.ServiceProvider;
import com.localservices.marketplace.repository.EmergencyRequestRepository;
import com.localservices.marketplace.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class EmergencyRequestService {

    private final EmergencyRequestRepository emergencyRepository;
    private final ServiceProviderRepository providerRepository;

    public EmergencyRequestService(
            EmergencyRequestRepository emergencyRepository,
            ServiceProviderRepository providerRepository) {

        this.emergencyRepository = emergencyRepository;
        this.providerRepository = providerRepository;
    }

    public EmergencyRequest createEmergencyRequest(
            EmergencyRequest request) {

        request.setStatus("PENDING");

        List<ServiceProvider> providers =
                providerRepository.findByCategory(
                        request.getServiceCategory());

        /*
         * Only providers who:
         * 1. Match the requested service category
         * 2. Are currently available
         * 3. Accept emergency requests
         * are considered.
         */
        providers.removeIf(provider ->
                !Boolean.TRUE.equals(provider.getAvailable())
                || !Boolean.TRUE.equals(
                        provider.getEmergencyAvailable()));

        /*
         * If both customer and provider have coordinates,
         * select the nearest suitable provider.
         */
        if (request.getLatitude() != null
                && request.getLongitude() != null) {

            providers.removeIf(provider ->
                    provider.getLatitude() == null
                    || provider.getLongitude() == null);

            if (!providers.isEmpty()) {

                ServiceProvider nearestProvider =
                        providers.stream()
                                .min(Comparator.comparingDouble(
                                        provider -> calculateDistance(
                                                request.getLatitude(),
                                                request.getLongitude(),
                                                provider.getLatitude(),
                                                provider.getLongitude()
                                        )))
                                .orElse(null);

                if (nearestProvider != null) {
                    request.setMatchedProviderId(
                            nearestProvider.getId());
                    request.setStatus("MATCHED");
                }
            }

        } else if (!providers.isEmpty()) {

            /*
             * If GPS coordinates are unavailable, select a suitable
             * available provider from the requested category.
             */
            ServiceProvider selectedProvider =
                    providers.get(0);

            request.setMatchedProviderId(
                    selectedProvider.getId());

            request.setStatus("MATCHED");
        }

        return emergencyRepository.save(request);
    }

    public List<EmergencyRequest> getCustomerRequests(
            Integer customerId) {

        return emergencyRepository.findByCustomerId(customerId);
    }

    public List<EmergencyRequest> getProviderRequests(
            Integer providerId) {

        return emergencyRepository
                .findByMatchedProviderId(providerId);
    }

    public EmergencyRequest updateStatus(
            Integer requestId,
            String status) {

        EmergencyRequest request =
                emergencyRepository.findById(requestId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Emergency request not found"));

        request.setStatus(status.toUpperCase());

        return emergencyRepository.save(request);
    }

    /*
     * Calculates approximate distance between two
     * latitude/longitude points using the Haversine formula.
     * Result is in kilometres.
     */
    private double calculateDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final double EARTH_RADIUS = 6371.0;

        double latDistance =
                Math.toRadians(lat2 - lat1);

        double lonDistance =
                Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latDistance / 2)
                        * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2)
                        * Math.sin(lonDistance / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}