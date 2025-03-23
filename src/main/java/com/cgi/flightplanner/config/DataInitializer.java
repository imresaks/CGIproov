package com.cgi.flightplanner.config;

import com.cgi.flightplanner.model.Flight;
import com.cgi.flightplanner.service.FlightService;
import com.cgi.flightplanner.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FlightService flightService;
    private final SeatService seatService;

    @Autowired
    public DataInitializer(FlightService flightService, SeatService seatService) {
        this.flightService = flightService;
        this.seatService = seatService;
    }

    @Override
    public void run(String... args) {
        // Create sample flights
        List<Flight> flights = Arrays.asList(
            createFlight("EE101", "Tallinn", "London", LocalDate.now().plusDays(1), LocalTime.of(8, 30), LocalTime.of(10, 15), 149.99, "Boeing 737", 180),
            createFlight("EE102", "Tallinn", "Paris", LocalDate.now().plusDays(1), LocalTime.of(12, 0), LocalTime.of(14, 15), 179.99, "Airbus A320", 150),
            createFlight("EE103", "Tallinn", "Berlin", LocalDate.now().plusDays(2), LocalTime.of(9, 45), LocalTime.of(11, 0), 129.99, "Boeing 737", 180),
            createFlight("EE104", "Tallinn", "Stockholm", LocalDate.now().plusDays(2), LocalTime.of(14, 30), LocalTime.of(15, 15), 89.99, "Bombardier CRJ900", 90),
            createFlight("EE105", "Tallinn", "Helsinki", LocalDate.now().plusDays(1), LocalTime.of(7, 0), LocalTime.of(7, 40), 69.99, "ATR 72", 70),
            createFlight("EE106", "Tallinn", "Riga", LocalDate.now().plusDays(1), LocalTime.of(16, 30), LocalTime.of(17, 15), 59.99, "ATR 72", 70),
            createFlight("EE107", "Tallinn", "Copenhagen", LocalDate.now().plusDays(3), LocalTime.of(10, 15), LocalTime.of(11, 30), 119.99, "Airbus A320", 150),
            createFlight("EE108", "Tallinn", "Amsterdam", LocalDate.now().plusDays(3), LocalTime.of(13, 45), LocalTime.of(15, 30), 159.99, "Airbus A320", 150)
        );
        
        // Save flights to database
        for (Flight flight : flights) {
            Flight savedFlight = flightService.saveFlight(flight);
            
            // Generate random seats for each flight
            int rows = flight.getTotalSeats() / 6; // Assuming 6 seats per row
            seatService.generateRandomSeatsForFlight(savedFlight, rows, 6);
        }
    }
    
    private Flight createFlight(String flightNumber, String origin, String destination, 
                               LocalDate departureDate, LocalTime departureTime, LocalTime arrivalTime, 
                               Double price, String aircraftType, Integer totalSeats) {
        Flight flight = new Flight();
        flight.setFlightNumber(flightNumber);
        flight.setOrigin(origin);
        flight.setDestination(destination);
        flight.setDepartureDate(departureDate);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setPrice(price);
        flight.setAircraftType(aircraftType);
        flight.setTotalSeats(totalSeats);
        return flight;
    }
}