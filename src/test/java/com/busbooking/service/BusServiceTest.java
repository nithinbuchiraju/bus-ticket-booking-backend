package com.busbooking.service;

import com.busbooking.dto.BusRequest;
import com.busbooking.dto.BusResponse;
import com.busbooking.dto.SeatResponse;
import com.busbooking.entity.Bus;
import com.busbooking.entity.Seat;
import com.busbooking.exception.ResourceNotFoundException;
import com.busbooking.repository.BusRepository;
import com.busbooking.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BusServiceTest {

    @Mock BusRepository busRepository;
    @Mock SeatRepository seatRepository;
    @Captor ArgumentCaptor<List<Seat>> seatsCaptor;
    @InjectMocks BusService busService;

    private Bus bus;

    @BeforeEach
    void setUp() {
        bus = Bus.builder().id(1L).busName("Orange").busType("AC Sleeper")
                .source("Hyderabad").destination("Bangalore")
                .travelDate(LocalDate.now().plusDays(1))
                .departureTime(LocalTime.of(21, 0)).arrivalTime(LocalTime.of(5, 30))
                .fare(new BigDecimal("1200")).totalSeats(40).build();
    }

    @Test
    void create_generatesSeatsFourPerRow() {
        when(busRepository.save(any(Bus.class))).thenAnswer(inv -> {
            Bus b = inv.getArgument(0);
            b.setId(7L);
            return b;
        });
        BusRequest req = new BusRequest("Test", "AC", "A", "B", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(15, 0), new BigDecimal("300"), 6);

        BusResponse res = busService.create(req);

        verify(seatRepository).saveAll(seatsCaptor.capture());
        assertThat(seatsCaptor.getValue())
                .extracting(Seat::getSeatNumber)
                .containsExactly("A1", "A2", "A3", "A4", "B1", "B2");
        assertThat(seatsCaptor.getValue()).allMatch(s -> !s.getIsBooked());
        assertThat(res.id()).isEqualTo(7L);
        assertThat(res.totalSeats()).isEqualTo(6);
    }

    @Test
    void search_trimsInputAndReportsAvailableSeats() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(busRepository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndTravelDate("Hyderabad", "Bangalore", date))
                .thenReturn(List.of(bus));
        when(seatRepository.countByBusIdAndIsBookedFalse(1L)).thenReturn(12L);

        List<BusResponse> result = busService.search("  Hyderabad ", "Bangalore ", date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).busName()).isEqualTo("Orange");
        assertThat(result.get(0).availableSeats()).isEqualTo(12);
    }

    @Test
    void search_noMatches_returnsEmptyList() {
        assertThat(busService.search("X", "Y", LocalDate.now())).isEmpty();
    }

    @Test
    void getById_unknownBus_throwsNotFound() {
        when(busRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> busService.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSeats_mapsBookedFlag() {
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.findByBusIdOrderBySeatNumber(1L)).thenReturn(List.of(
                Seat.builder().id(1L).bus(bus).seatNumber("A1").isBooked(true).build(),
                Seat.builder().id(2L).bus(bus).seatNumber("A2").isBooked(false).build()));

        List<SeatResponse> seats = busService.getSeats(1L);

        assertThat(seats).extracting(SeatResponse::seatNumber).containsExactly("A1", "A2");
        assertThat(seats).extracting(SeatResponse::booked).containsExactly(true, false);
    }

    @Test
    void getSeats_unknownBus_throwsNotFound() {
        when(busRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> busService.getSeats(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
