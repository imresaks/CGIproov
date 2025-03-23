package com.cgi.flightplanner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String seatNumber;
    private boolean isWindow;
    private boolean isExitRow;
    private boolean hasExtraLegroom;
    private boolean isOccupied;
    private String seatClass; // ECONOMY, BUSINESS, FIRST
    
    @ManyToOne
    private Flight flight;
}