package com.busbooking.controller;

import com.busbooking.dto.BookingRequest;
import com.busbooking.dto.BookingResponse;
import com.busbooking.exception.GlobalExceptionHandler;
import com.busbooking.exception.SeatAlreadyBookedException;
import com.busbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private static final String EMAIL = "ravi@test.com";

    @Mock BookingService bookingService;
    @InjectMocks BookingController controller;
    MockMvc mockMvc;
    Principal principal = new UsernamePasswordAuthenticationToken(EMAIL, null);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private BookingResponse sample(String status) {
        return new BookingResponse(1L, 10L, "Orange", "Hyderabad", "Bangalore", null, null,
                "A1", "Asha", 25, status, new BigDecimal("500"), null);
    }

    private static final String VALID_BODY =
            "{\"busId\":10,\"passengers\":[{\"seatId\":1,\"passengerName\":\"Asha\",\"passengerAge\":25}]}";

    @Test
    void book_validRequest_returns201AndUsesLoggedInUser() throws Exception {
        when(bookingService.book(eq(EMAIL), any(BookingRequest.class))).thenReturn(List.of(sample("CONFIRMED")));

        mockMvc.perform(post("/api/bookings").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].seatNumber").value("A1"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void book_noPassengers_returns400() throws Exception {
        mockMvc.perform(post("/api/bookings").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"busId\":10,\"passengers\":[]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void book_blankPassengerName_returns400() throws Exception {
        mockMvc.perform(post("/api/bookings").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"busId\":10,\"passengers\":[{\"seatId\":1,\"passengerName\":\"\",\"passengerAge\":25}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void book_seatTaken_returns409() throws Exception {
        when(bookingService.book(eq(EMAIL), any(BookingRequest.class)))
                .thenThrow(new SeatAlreadyBookedException("Seat A1 is already booked"));

        mockMvc.perform(post("/api/bookings").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Seat A1 is already booked"));
    }

    @Test
    void myBookings_returnsList() throws Exception {
        when(bookingService.myBookings(EMAIL)).thenReturn(List.of(sample("CONFIRMED"), sample("CANCELLED")));

        mockMvc.perform(get("/api/bookings/my").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].status").value("CANCELLED"));
    }

    @Test
    void cancel_returnsUpdatedBooking() throws Exception {
        when(bookingService.cancel(EMAIL, 1L)).thenReturn(sample("CANCELLED"));

        mockMvc.perform(put("/api/bookings/1/cancel").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(bookingService).cancel(EMAIL, 1L);
    }
}
