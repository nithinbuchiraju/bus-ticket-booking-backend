package com.busbooking.service;

import com.busbooking.dto.BookingRequest;
import com.busbooking.dto.BookingResponse;
import com.busbooking.entity.*;
import com.busbooking.exception.ResourceNotFoundException;
import com.busbooking.exception.SeatAlreadyBookedException;
import com.busbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BusRepository busRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    /** Books every requested seat in one transaction: all succeed or none do. */
    @Transactional
    public List<BookingResponse> book(String email, BookingRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Bus bus = busRepository.findById(req.busId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id " + req.busId()));

        if (bus.getTravelDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot book a bus that has already departed");
        }

        List<Booking> created = new ArrayList<>();
        for (BookingRequest.PassengerSeat p : req.passengers()) {
            Seat seat = seatRepository.findById(p.seatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id " + p.seatId()));
            if (!seat.getBus().getId().equals(bus.getId())) {
                throw new IllegalArgumentException("Seat " + seat.getSeatNumber() + " does not belong to this bus");
            }
            if (Boolean.TRUE.equals(seat.getIsBooked())) {
                throw new SeatAlreadyBookedException("Seat " + seat.getSeatNumber() + " is already booked");
            }
            seat.setIsBooked(true);
            seatRepository.save(seat);

            created.add(bookingRepository.save(Booking.builder()
                    .user(user).bus(bus).seat(seat)
                    .passengerName(p.passengerName().trim())
                    .passengerAge(p.passengerAge())
                    .status(BookingStatus.CONFIRMED)
                    .totalAmount(bus.getFare())
                    .build()));
        }
        return created.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return bookingRepository.findByUserIdOrderByBookingDateDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<BookingResponse> allBookings() {
    return bookingRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
}

    @Transactional
    public BookingResponse cancel(String email, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));

        if (!booking.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Booking not found with id " + bookingId);
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }
        if (booking.getBus().getTravelDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot cancel a booking for a past trip");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Seat seat = booking.getSeat();
        seat.setIsBooked(false);
        seatRepository.save(seat);
        return toResponse(bookingRepository.save(booking));
    }

    private BookingResponse toResponse(Booking b) {
        Bus bus = b.getBus();
        return new BookingResponse(b.getId(), bus.getId(), bus.getBusName(), bus.getSource(),
                bus.getDestination(), bus.getTravelDate(), bus.getDepartureTime(),
                b.getSeat().getSeatNumber(), b.getPassengerName(), b.getPassengerAge(),
                b.getStatus().name(), b.getTotalAmount(), b.getBookingDate());
    }
}
