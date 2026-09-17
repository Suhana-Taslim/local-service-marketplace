package com.localservices.marketplace.service;

import com.localservices.marketplace.model.Booking;
import com.localservices.marketplace.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking createBooking(Booking booking) {

        if (booking.getStatus() == null) {
            booking.setStatus("PENDING");
        }

        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByCustomer(Integer customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<Booking> getBookingsByService(Integer serviceId) {
        return bookingRepository.findByServiceId(serviceId);
    }

    public Booking updateBookingStatus(Integer bookingId, String status) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(status);

        return bookingRepository.save(booking);
    }
}