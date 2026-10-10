package com.busbooking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"bus_id", "seat_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @Column(nullable = false)
    private String seatNumber;         // e.g. A1, A2, B1

    @Column(nullable = false)
    @Builder.Default
    private Boolean isBooked = false;

    @Version
    private Long version;              // optimistic locking, prevents double booking
}
