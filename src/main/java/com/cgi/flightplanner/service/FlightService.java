package com.cgi.flightplanner.service;

import com.cgi.flightplanner.model.Flight;
import com.cgi.flightplanner.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    private final FlightRepository flightRepository;

    @Autowired
    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    public List<Flight> getFlightsByDestination(String destination) {
        return flightRepository.findByDestination(destination);
    }

    public List<Flight> getFlightsByDepartureDate(LocalDate departureDate) {
        return flightRepository.findByDepartureDate(departureDate);
    }

    public List<Flight> getFlightsByDepartureDateAndDestination(LocalDate departureDate, String destination) {
        return flightRepository.findByDepartureDateAndDestination(departureDate, destination);
    }

    public List<Flight> getFlightsByDepartureTimeBetween(LocalTime startTime, LocalTime endTime) {
        return flightRepository.findByDepartureTimeBetween(startTime, endTime);
    }

    public List<Flight> getFlightsByMaxPrice(Double maxPrice) {
        return flightRepository.findByPriceLessThanEqual(maxPrice);
    }

    public Flight saveFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }
}