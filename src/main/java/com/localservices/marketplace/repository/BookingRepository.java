package com.localservices.marketplace.repository;

import com.localservices.marketplace.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByCustomerId(Integer customerId);

    List<Booking> findByServiceId(Integer serviceId);
}