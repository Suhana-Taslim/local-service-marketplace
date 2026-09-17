package com.localservices.marketplace.controller;

import com.localservices.marketplace.model.Booking;
import com.localservices.marketplace.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        return ResponseEntity.ok(bookingService.createBooking(booking));
    }

    @GetMapping("/customer/{customerId}")
    public List<Booking> getByCustomer(@PathVariable Integer customerId) {
        return bookingService.getBookingsByCustomer(customerId);
    }

    @GetMapping("/service/{serviceId}")
    public List<Booking> getByService(@PathVariable Integer serviceId) {
        return bookingService.getBookingsByService(serviceId);
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<Booking> updateBookingStatus(
            @PathVariable Integer bookingId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                bookingService.updateBookingStatus(bookingId, status)
        );
    }
}