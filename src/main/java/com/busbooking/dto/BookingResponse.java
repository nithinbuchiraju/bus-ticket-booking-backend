package com.busbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BookingResponse(
        Long id, Long busId, String busName, String source, String destination,
        LocalDate travelDate, LocalTime departureTime, String seatNumber,
        String passengerName, Integer passengerAge, String status,
        BigDecimal totalAmount, LocalDateTime bookingDate) {}
