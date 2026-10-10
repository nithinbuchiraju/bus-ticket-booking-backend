package com.busbooking.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record BusRequest(
        @NotBlank String busName,
        String busType,
        @NotBlank String source,
        @NotBlank String destination,
        @NotNull LocalDate travelDate,
        LocalTime departureTime,
        LocalTime arrivalTime,
        @NotNull @Positive BigDecimal fare,
        @NotNull @Min(1) @Max(100) Integer totalSeats) {}
