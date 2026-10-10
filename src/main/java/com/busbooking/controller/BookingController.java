package com.busbooking.controller;

import com.busbooking.dto.BookingRequest;
import com.busbooking.dto.BookingResponse;
import com.busbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<List<BookingResponse>> book(@Valid @RequestBody BookingRequest req,
                                                      Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.book(auth.getName(), req));
    }

    @GetMapping("/my")
    public List<BookingResponse> myBookings(Authentication auth) {
        return bookingService.myBookings(auth.getName());
    }

    @PutMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id, Authentication auth) {
        return bookingService.cancel(auth.getName(), id);
    }
}
