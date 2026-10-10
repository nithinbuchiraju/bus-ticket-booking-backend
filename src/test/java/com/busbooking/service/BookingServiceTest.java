package com.busbooking.service;

import com.busbooking.dto.BookingRequest;
import com.busbooking.dto.BookingResponse;
import com.busbooking.entity.*;
import com.busbooking.exception.ResourceNotFoundException;
import com.busbooking.exception.SeatAlreadyBookedException;
import com.busbooking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock BusRepository busRepository;
    @Mock SeatRepository seatRepository;
    @Mock UserRepository userRepository;
    @InjectMocks BookingService bookingService;

    private static final String EMAIL = "ravi@test.com";
    private User user;
    private Bus bus;
    private final AtomicLong ids = new AtomicLong(100);

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Ravi").email(EMAIL).password("x").build();
        bus = Bus.builder().id(10L).busName("Orange").source("Hyderabad").destination("Bangalore")
                .travelDate(LocalDate.now().plusDays(1)).departureTime(LocalTime.of(21, 0))
                .fare(new BigDecimal("500")).totalSeats(40).build();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(busRepository.findById(10L)).thenReturn(Optional.of(bus));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) b.setId(ids.incrementAndGet());
            return b;
        });
    }

    private Seat seat(long id, String number, boolean booked, Bus owner) {
        Seat s = Seat.builder().id(id).bus(owner).seatNumber(number).isBooked(booked).build();
        when(seatRepository.findById(id)).thenReturn(Optional.of(s));
        return s;
    }

    private BookingRequest request(BookingRequest.PassengerSeat... passengers) {
        return new BookingRequest(10L, List.of(passengers));
    }

    private Booking confirmedBooking(Seat seat) {
        Booking b = Booking.builder().id(5L).user(user).bus(bus).seat(seat)
                .passengerName("Asha").passengerAge(25)
                .status(BookingStatus.CONFIRMED).totalAmount(bus.getFare()).build();
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(b));
        return b;
    }

    // ---------- book ----------

    @Test
    void book_multipleSeats_marksSeatsBookedAndCreatesConfirmedBookings() {
        Seat s1 = seat(1L, "A1", false, bus);
        Seat s2 = seat(2L, "A2", false, bus);

        List<BookingResponse> res = bookingService.book(EMAIL, request(
                new BookingRequest.PassengerSeat(1L, " Asha ", 25),
                new BookingRequest.PassengerSeat(2L, "Kiran", 30)));

        assertThat(res).hasSize(2);
        assertThat(res).extracting(BookingResponse::seatNumber).containsExactly("A1", "A2");
        assertThat(res).extracting(BookingResponse::passengerName).containsExactly("Asha", "Kiran");
        assertThat(res).allMatch(r -> r.status().equals("CONFIRMED"));
        assertThat(res.get(0).totalAmount()).isEqualByComparingTo("500");
        assertThat(s1.getIsBooked()).isTrue();
        assertThat(s2.getIsBooked()).isTrue();
    }

    @Test
    void book_alreadyBookedSeat_throwsConflictAndCreatesNoBooking() {
        seat(1L, "A1", true, bus);

        assertThatThrownBy(() -> bookingService.book(EMAIL, request(new BookingRequest.PassengerSeat(1L, "Asha", 25))))
                .isInstanceOf(SeatAlreadyBookedException.class)
                .hasMessageContaining("A1");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void book_seatBelongingToAnotherBus_isRejected() {
        Bus other = Bus.builder().id(99L).build();
        seat(1L, "A1", false, other);

        assertThatThrownBy(() -> bookingService.book(EMAIL, request(new BookingRequest.PassengerSeat(1L, "Asha", 25))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void book_pastTrip_isRejected() {
        bus.setTravelDate(LocalDate.now().minusDays(1));
        seat(1L, "A1", false, bus);

        assertThatThrownBy(() -> bookingService.book(EMAIL, request(new BookingRequest.PassengerSeat(1L, "Asha", 25))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already departed");
    }

    @Test
    void book_unknownBus_throwsNotFound() {
        when(busRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.book(EMAIL, request(new BookingRequest.PassengerSeat(1L, "Asha", 25))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void book_unknownSeat_throwsNotFound() {
        when(seatRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.book(EMAIL, request(new BookingRequest.PassengerSeat(404L, "Asha", 25))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- my bookings ----------

    @Test
    void myBookings_returnsUsersBookings() {
        Seat s = seat(1L, "A1", true, bus);
        Booking b = confirmedBooking(s);
        when(bookingRepository.findByUserIdOrderByBookingDateDesc(1L)).thenReturn(List.of(b));

        List<BookingResponse> res = bookingService.myBookings(EMAIL);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).busName()).isEqualTo("Orange");
        assertThat(res.get(0).seatNumber()).isEqualTo("A1");
    }

    // ---------- cancel ----------

    @Test
    void cancel_confirmedBooking_freesSeatAndMarksCancelled() {
        Seat s = seat(1L, "A1", true, bus);
        confirmedBooking(s);

        BookingResponse res = bookingService.cancel(EMAIL, 5L);

        assertThat(res.status()).isEqualTo("CANCELLED");
        assertThat(s.getIsBooked()).isFalse();
    }

    @Test
    void cancel_someoneElsesBooking_isTreatedAsNotFound() {
        confirmedBooking(seat(1L, "A1", true, bus));

        assertThatThrownBy(() -> bookingService.cancel("other@test.com", 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancel_alreadyCancelled_isRejected() {
        Booking b = confirmedBooking(seat(1L, "A1", false, bus));
        b.setStatus(BookingStatus.CANCELLED);

        assertThatThrownBy(() -> bookingService.cancel(EMAIL, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void cancel_pastTrip_isRejected() {
        confirmedBooking(seat(1L, "A1", true, bus));
        bus.setTravelDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> bookingService.cancel(EMAIL, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past trip");
    }

    @Test
    void cancel_unknownBooking_throwsNotFound() {
        when(bookingRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancel(EMAIL, 404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
