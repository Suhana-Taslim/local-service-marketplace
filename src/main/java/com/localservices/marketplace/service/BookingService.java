package com.localservices.marketplace.service;

import com.localservices.marketplace.model.Booking;
import com.localservices.marketplace.repository.BookingRepository;
import com.localservices.marketplace.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@org.springframework.stereotype.Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;

    public BookingService(
            BookingRepository bookingRepository,
            ServiceRepository serviceRepository) {
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking createBooking(Booking booking) {

        if (booking.getStatus() == null || booking.getStatus().isBlank()) {
            booking.setStatus("PENDING");
        }

        /*
         * Automatically identify the provider from the selected service.
         * This prevents the customer from having to manually enter
         * a provider ID.
         */
        if (booking.getServiceId() != null) {

            com.localservices.marketplace.model.Service service =
        serviceRepository.findById(booking.getServiceId())
                    .orElseThrow(() ->
                            new RuntimeException("Service not found"));

            booking.setProviderId(service.getProviderId());
        }

        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByCustomer(Integer customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<Booking> getBookingsByService(Integer serviceId) {
        return bookingRepository.findByServiceId(serviceId);
    }

    public List<Booking> getBookingsByProvider(Integer providerId) {
        return bookingRepository.findByProviderId(providerId);
    }

    public Booking updateBookingStatus(Integer bookingId, String status) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        String newStatus = status.toUpperCase();

        if (!newStatus.equals("PENDING")
                && !newStatus.equals("ACCEPTED")
                && !newStatus.equals("REJECTED")
                && !newStatus.equals("COMPLETED")
                && !newStatus.equals("CANCELLED")) {

            throw new IllegalArgumentException(
                    "Invalid booking status");
        }

        booking.setStatus(newStatus);

        return bookingRepository.save(booking);
    }
}