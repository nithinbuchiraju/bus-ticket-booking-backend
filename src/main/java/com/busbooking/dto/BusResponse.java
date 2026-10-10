package com.busbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record BusResponse(
        Long id, String busName, String busType, String source, String destination,
        LocalDate travelDate, LocalTime departureTime, LocalTime arrivalTime,
        BigDecimal fare, Integer totalSeats, long availableSeats) {}
