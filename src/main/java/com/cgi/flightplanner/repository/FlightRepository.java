package com.cgi.flightplanner.repository;

import com.cgi.flightplanner.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    
    List<Flight> findByDestination(String destination);
    
    List<Flight> findByDepartureDate(LocalDate departureDate);
    
    List<Flight> findByDepartureDateAndDestination(LocalDate departureDate, String destination);
    
    @Query("SELECT f FROM Flight f WHERE f.departureTime >= ?1 AND f.departureTime <= ?2")
    List<Flight> findByDepartureTimeBetween(LocalTime startTime, LocalTime endTime);
    
    @Query("SELECT f FROM Flight f WHERE f.price <= ?1")
    List<Flight> findByPriceLessThanEqual(Double maxPrice);
}