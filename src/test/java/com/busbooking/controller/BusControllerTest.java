package com.busbooking.controller;

import com.busbooking.dto.BusResponse;
import com.busbooking.dto.SeatResponse;
import com.busbooking.exception.GlobalExceptionHandler;
import com.busbooking.exception.ResourceNotFoundException;
import com.busbooking.service.BusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BusControllerTest {

    @Mock BusService busService;
    @InjectMocks BusController controller;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void search_returnsMatchingBuses() throws Exception {
        LocalDate date = LocalDate.of(2026, 10, 15);
        BusResponse bus = new BusResponse(1L, "Orange", "AC Sleeper", "Hyderabad", "Bangalore",
                date, null, null, new BigDecimal("1200"), 40, 25);
        when(busService.search("Hyderabad", "Bangalore", date)).thenReturn(List.of(bus));

        mockMvc.perform(get("/api/buses/search")
                        .param("source", "Hyderabad").param("destination", "Bangalore").param("date", "2026-10-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].busName").value("Orange"))
                .andExpect(jsonPath("$[0].availableSeats").value(25));
    }

    @Test
    void search_missingDate_returns400() throws Exception {
        mockMvc.perform(get("/api/buses/search").param("source", "A").param("destination", "B"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBus_unknownId_returns404() throws Exception {
        when(busService.getById(99L)).thenThrow(new ResourceNotFoundException("Bus not found with id 99"));

        mockMvc.perform(get("/api/buses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bus not found with id 99"));
    }

    @Test
    void getSeats_returnsSeatList() throws Exception {
        when(busService.getSeats(1L)).thenReturn(List.of(new SeatResponse(1L, "A1", true), new SeatResponse(2L, "A2", false)));

        mockMvc.perform(get("/api/buses/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value("A1"))
                .andExpect(jsonPath("$[0].booked").value(true))
                .andExpect(jsonPath("$[1].booked").value(false));
    }
}
