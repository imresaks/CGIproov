package com.cgi.flightplanner.controller;

import com.cgi.flightplanner.model.Flight;
import com.cgi.flightplanner.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class FlightController {

    private final FlightService flightService;

    @Autowired
    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Flight> flights = flightService.getAllFlights();
        model.addAttribute("flights", flights);
        return "index";
    }

    @GetMapping("/flights")
    public String getAllFlights(Model model) {
        List<Flight> flights = flightService.getAllFlights();
        model.addAttribute("flights", flights);
        return "index";
    }

    @GetMapping("/flights/search")
    public String searchFlights(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Double maxPrice,
            Model model) {

        List<Flight> flights;

        // Apply filters based on provided parameters
        if (destination != null && !destination.isEmpty() && departureDate != null) {
            flights = flightService.getFlightsByDepartureDateAndDestination(departureDate, destination);
        } else if (departureDate != null) {
            flights = flightService.getFlightsByDepartureDate(departureDate);
        } else if (destination != null && !destination.isEmpty()) {
            flights = flightService.getFlightsByDestination(destination);
        } else if (startTime != null && endTime != null) {
            flights = flightService.getFlightsByDepartureTimeBetween(startTime, endTime);
        } else if (maxPrice != null) {
            flights = flightService.getFlightsByMaxPrice(maxPrice);
        } else {
            flights = flightService.getAllFlights();
        }

        model.addAttribute("flights", flights);
        return "index";
    }
}