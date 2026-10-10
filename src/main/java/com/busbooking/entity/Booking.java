package com.busbooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Column(nullable = false)
    private String passengerName;

    private Integer passengerAge;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @PrePersist
    void onCreate() {
        if (bookingDate == null) bookingDate = LocalDateTime.now();
    }
}
