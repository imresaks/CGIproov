package com.cgi.flightplanner.controller;

import com.cgi.flightplanner.model.Flight;
import com.cgi.flightplanner.model.Seat;
import com.cgi.flightplanner.service.FlightService;
import com.cgi.flightplanner.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class SeatController {

    private final SeatService seatService;
    private final FlightService flightService;

    @Autowired
    public SeatController(SeatService seatService, FlightService flightService) {
        this.seatService = seatService;
        this.flightService = flightService;
    }

    @GetMapping("/seats/{id}")
    public String getSeatMap(@PathVariable Long id, Model model) {
        Optional<Flight> flightOptional = flightService.getFlightById(id);
        
        if (flightOptional.isPresent()) {
            Flight flight = flightOptional.get();
            List<Seat> seats = seatService.getAvailableSeatsByFlight(flight);
            
            model.addAttribute("flight", flight);
            model.addAttribute("seats", seats);
            return "seats";
        } else {
            return "redirect:/flights";
        }
    }

    @GetMapping("/seats/{id}/recommend")
    public String recommendSeats(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean wantWindow,
            @RequestParam(required = false, defaultValue = "false") boolean wantExitRow,
            @RequestParam(required = false, defaultValue = "false") boolean wantExtraLegroom,
            @RequestParam(required = false) String seatClass,
            @RequestParam(required = false, defaultValue = "1") int numberOfSeats,
            Model model) {
        
        Optional<Flight> flightOptional = flightService.getFlightById(id);
        
        if (flightOptional.isPresent()) {
            Flight flight = flightOptional.get();
            List<Seat> seats = seatService.getAvailableSeatsByFlight(flight);
            List<Seat> recommendedSeats = seatService.recommendSeats(flight, wantWindow, wantExitRow, wantExtraLegroom, seatClass, numberOfSeats);
            
            // Prepare recommendation details
            boolean areConsecutive = false;
            if (recommendedSeats.size() > 1) {
                areConsecutive = true;
                for (int i = 0; i < recommendedSeats.size() - 1; i++) {
                    if (!areSeatsConsecutive(recommendedSeats.get(i), recommendedSeats.get(i + 1))) {
                        areConsecutive = false;
                        break;
                    }
                }
            }
            
            // Count how many preferences were matched
            int matchedPreferences = 0;
            boolean hasWindow = false;
            boolean hasExitRow = false;
            boolean hasExtraLegroom = false;
            
            for (Seat seat : recommendedSeats) {
                if (seat.isWindow()) hasWindow = true;
                if (seat.isExitRow()) hasExitRow = true;
                if (seat.hasExtraLegroom()) hasExtraLegroom = true;
            }
            
            if (wantWindow && hasWindow) matchedPreferences++;
            if (wantExitRow && hasExitRow) matchedPreferences++;
            if (wantExtraLegroom && hasExtraLegroom) matchedPreferences++;
            
            int totalPreferences = (wantWindow ? 1 : 0) + (wantExitRow ? 1 : 0) + (wantExtraLegroom ? 1 : 0);
            
            model.addAttribute("flight", flight);
            model.addAttribute("seats", seats);
            model.addAttribute("recommendedSeats", recommendedSeats);
            model.addAttribute("wantWindow", wantWindow);
            model.addAttribute("wantExitRow", wantExitRow);
            model.addAttribute("wantExtraLegroom", wantExtraLegroom);
            model.addAttribute("seatClass", seatClass);
            model.addAttribute("numberOfSeats", numberOfSeats);
            model.addAttribute("areConsecutive", areConsecutive);
            model.addAttribute("matchedPreferences", matchedPreferences);
            model.addAttribute("totalPreferences", totalPreferences);
            model.addAttribute("hasWindow", hasWindow);
            model.addAttribute("hasExitRow", hasExitRow);
            model.addAttribute("hasExtraLegroom", hasExtraLegroom);
            
            return "seats";
        } else {
            return "redirect:/flights";
        }
    }
    
    private boolean areSeatsConsecutive(Seat seat1, Seat seat2) {
        // Extract row numbers and seat letters
        String num1 = seat1.getSeatNumber().replaceAll("[^0-9]", "");
        String num2 = seat2.getSeatNumber().replaceAll("[^0-9]", "");
        String letter1 = seat1.getSeatNumber().replaceAll("[0-9]", "");
        String letter2 = seat2.getSeatNumber().replaceAll("[0-9]", "");
        
        // Check if they're in the same row
        if (num1.equals(num2)) {
            char l1 = letter1.charAt(0);
            char l2 = letter2.charAt(0);
            
            // Check if seats are adjacent, considering the aisle between C and D
            if (Math.abs(l1 - l2) == 1) {
                // Make sure we're not considering seats across the aisle (C and D) as consecutive
                return !((l1 == 'C' && l2 == 'D') || (l1 == 'D' && l2 == 'C'));
            }
        }
        
        return false;
}

}