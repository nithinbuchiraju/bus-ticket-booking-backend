package com.busbooking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record BookingRequest(
        @NotNull(message = "Bus is required") Long busId,
        @NotEmpty(message = "Select at least one seat") @Valid List<PassengerSeat> passengers) {

    public record PassengerSeat(
            @NotNull Long seatId,
            @NotBlank(message = "Passenger name is required") String passengerName,
            @Min(1) @Max(120) Integer passengerAge) {}
}
