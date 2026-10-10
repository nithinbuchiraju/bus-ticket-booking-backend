package com.busbooking.repository;

import com.busbooking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByBusIdOrderBySeatNumber(Long busId);
    long countByBusIdAndIsBookedFalse(Long busId);
}
