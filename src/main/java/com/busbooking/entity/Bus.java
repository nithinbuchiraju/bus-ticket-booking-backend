package com.busbooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "buses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String busName;

    private String busType;            // AC Sleeper, Non-AC Seater, etc.

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDate travelDate;

    private LocalTime departureTime;
    private LocalTime arrivalTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fare;

    @Column(nullable = false)
    private Integer totalSeats;
}
