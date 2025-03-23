package com.cgi.flightplanner.repository;

import com.cgi.flightplanner.model.Flight;
import com.cgi.flightplanner.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByFlightAndIsOccupiedFalse(Flight flight);
    
    List<Seat> findByFlightAndIsWindowAndIsOccupiedFalse(Flight flight, boolean isWindow);
    
    List<Seat> findByFlightAndIsExitRowAndIsOccupiedFalse(Flight flight, boolean isExitRow);
    
    List<Seat> findByFlightAndHasExtraLegroomAndIsOccupiedFalse(Flight flight, boolean hasExtraLegroom);
    
    List<Seat> findByFlightAndSeatClassAndIsOccupiedFalse(Flight flight, String seatClass);
    
    @Query("SELECT s FROM Seat s WHERE s.flight = ?1 AND s.isOccupied = false ORDER BY s.id")
    List<Seat> findAvailableSeatsSortedById(Flight flight);
}