
package com.busbooking.service;

import com.busbooking.dto.BusRequest;
import com.busbooking.dto.BusResponse;
import com.busbooking.dto.SeatResponse;
import com.busbooking.entity.Bus;
import com.busbooking.entity.Seat;
import com.busbooking.exception.ResourceNotFoundException;
import com.busbooking.repository.BusRepository;
import com.busbooking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public List<BusResponse> getAllBuses() {
        return busRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BusResponse> search(String source, String destination, LocalDate date) {
        return busRepository
                .findBySourceIgnoreCaseAndDestinationIgnoreCaseAndTravelDate(source.trim(), destination.trim(), date)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BusResponse getById(Long id) {
        return toResponse(findBus(id));
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(Long busId) {
        findBus(busId);
        return seatRepository.findByBusIdOrderBySeatNumber(busId).stream()
                .map(s -> new SeatResponse(s.getId(), s.getSeatNumber(), Boolean.TRUE.equals(s.getIsBooked())))
                .toList();
    }

    /** Creates a bus and generates its seats (4 per row: A1..A4, B1..B4, ...). */
    @Transactional
    public BusResponse create(BusRequest r) {
        Bus bus = busRepository.save(Bus.builder()
                .busName(r.busName()).busType(r.busType())
                .source(r.source()).destination(r.destination())
                .travelDate(r.travelDate())
                .departureTime(r.departureTime()).arrivalTime(r.arrivalTime())
                .fare(r.fare()).totalSeats(r.totalSeats())
                .build());

        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < r.totalSeats(); i++) {
            char row = (char) ('A' + i / 4);
            int col = i % 4 + 1;
            seats.add(Seat.builder().bus(bus).seatNumber("" + row + col).isBooked(false).build());
        }
        seatRepository.saveAll(seats);
        return toResponse(bus);
    }

    private Bus findBus(Long id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id " + id));
    }

    private BusResponse toResponse(Bus b) {
        return new BusResponse(b.getId(), b.getBusName(), b.getBusType(), b.getSource(), b.getDestination(),
                b.getTravelDate(), b.getDepartureTime(), b.getArrivalTime(), b.getFare(), b.getTotalSeats(),
                seatRepository.countByBusIdAndIsBookedFalse(b.getId()));
    }
}
