package com.busbooking.controller;

import com.busbooking.dto.BusRequest;
import com.busbooking.dto.BusResponse;
import com.busbooking.dto.SeatResponse;
import com.busbooking.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    @GetMapping("/search")
    public List<BusResponse> search(@RequestParam String source,
                                    @RequestParam String destination,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return busService.search(source, destination, date);
    }

    @GetMapping("/{id}")
    public BusResponse getBus(@PathVariable Long id) {
        return busService.getById(id);
    }

    @GetMapping("/{id}/seats")
    public List<SeatResponse> getSeats(@PathVariable Long id) {
        return busService.getSeats(id);
    }

    /** ADMIN only (enforced in SecurityConfig). */
    @PostMapping
    public ResponseEntity<BusResponse> create(@Valid @RequestBody BusRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(busService.create(req));
    }
}
