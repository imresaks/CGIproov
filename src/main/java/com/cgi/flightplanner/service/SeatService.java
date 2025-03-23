package com.cgi.flightplanner.service;

import com.cgi.flightplanner.model.Flight;
import com.cgi.flightplanner.model.Seat;
import com.cgi.flightplanner.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public Optional<Seat> getSeatById(Long id) {
        return seatRepository.findById(id);
    }

    public List<Seat> getAvailableSeatsByFlight(Flight flight) {
        return seatRepository.findByFlightAndIsOccupiedFalse(flight);
    }

    public List<Seat> getAvailableWindowSeatsByFlight(Flight flight) {
        return seatRepository.findByFlightAndIsWindowAndIsOccupiedFalse(flight, true);
    }

    public List<Seat> getAvailableExitRowSeatsByFlight(Flight flight) {
        return seatRepository.findByFlightAndIsExitRowAndIsOccupiedFalse(flight, true);
    }

    public List<Seat> getAvailableExtraLegroomSeatsByFlight(Flight flight) {
        return seatRepository.findByFlightAndHasExtraLegroomAndIsOccupiedFalse(flight, true);
    }

    public List<Seat> getAvailableSeatsByFlightAndClass(Flight flight, String seatClass) {
        return seatRepository.findByFlightAndSeatClassAndIsOccupiedFalse(flight, seatClass);
    }

    public List<Seat> recommendSeats(Flight flight, boolean wantWindow, boolean wantExitRow, 
                                    boolean wantExtraLegroom, String seatClass, int numberOfSeats) {
        List<Seat> availableSeats = seatRepository.findByFlightAndIsOccupiedFalse(flight);
        List<Seat> recommendedSeats = new ArrayList<>();
        
        // Filter by seat class if specified
        if (seatClass != null && !seatClass.isEmpty()) {
            availableSeats.removeIf(seat -> !seat.getSeatClass().equals(seatClass));
        }
        
        // Instead of filtering out seats that don't match preferences,
        // we'll score each seat based on how well it matches preferences
        Map<Seat, Integer> seatScores = new HashMap<>();
        
        for (Seat seat : availableSeats) {
            int score = 0;
            
            // Add points for matching preferences
            if (wantWindow && seat.isWindow()) {
                score += 3;
            }
            
            if (wantExitRow && seat.isExitRow()) {
                score += 3;
            }
            
            if (wantExtraLegroom && seat.hasExtraLegroom()) {
                score += 3;
            }
            
            // Add base points for seat class quality
            if (seat.getSeatClass().equals("FIRST")) {
                score += 2;
            } else if (seat.getSeatClass().equals("BUSINESS")) {
                score += 1;
            }
            
            // Store the score
            seatScores.put(seat, score);
        }
        
        // Sort seats by score (highest first)
        List<Seat> scoredSeats = new ArrayList<>(availableSeats);
        scoredSeats.sort((s1, s2) -> seatScores.getOrDefault(s2, 0) - seatScores.getOrDefault(s1, 0));
        
        // If no preferences were specified, just use the available seats
        List<Seat> filteredSeats = (!wantWindow && !wantExitRow && !wantExtraLegroom) 
            ? availableSeats : scoredSeats;
        
        // Try to find consecutive seats if multiple seats are requested
        if (numberOfSeats > 1 && filteredSeats.size() >= numberOfSeats) {
            // First try to find consecutive seats from our scored and filtered seats
            List<Seat> consecutiveSeats = findBestConsecutiveSeats(filteredSeats, numberOfSeats);
            
            if (!consecutiveSeats.isEmpty()) {
                return consecutiveSeats;
            }
            
            // If we couldn't find consecutive seats with all preferences, try with just the seat class filter
            if (wantWindow || wantExitRow || wantExtraLegroom) {
                List<Seat> seatClassFiltered = new ArrayList<>(availableSeats);
                if (seatClass != null && !seatClass.isEmpty()) {
                    seatClassFiltered.removeIf(seat -> !seat.getSeatClass().equals(seatClass));
                }
                
                consecutiveSeats = findBestConsecutiveSeats(seatClassFiltered, numberOfSeats);
                if (!consecutiveSeats.isEmpty()) {
                    return consecutiveSeats;
                }
            }
        }
        
        // If we couldn't find consecutive seats or only need one seat,
        // return the best individual seats based on score
        for (int i = 0; i < Math.min(numberOfSeats, filteredSeats.size()); i++) {
            recommendedSeats.add(filteredSeats.get(i));
        }
        
        return recommendedSeats;
    }
    
    private boolean areConsecutiveSeats(Seat seat1, Seat seat2) {
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
    
    /**
     * Finds the best group of consecutive seats based on user preferences
     * @param seats List of available seats sorted by preference score
     * @param numberOfSeats Number of consecutive seats needed
     * @return List of consecutive seats, or empty list if none found
     */
    private List<Seat> findBestConsecutiveSeats(List<Seat> seats, int numberOfSeats) {
        // Group seats by row
        Map<String, List<Seat>> seatsByRow = new HashMap<>();
        
        for (Seat seat : seats) {
            String rowNum = seat.getSeatNumber().replaceAll("[^0-9]", "");
            if (!seatsByRow.containsKey(rowNum)) {
                seatsByRow.put(rowNum, new ArrayList<>());
            }
            seatsByRow.get(rowNum).add(seat);
        }
        
        // For each row, find consecutive seats
        for (String row : seatsByRow.keySet()) {
            List<Seat> rowSeats = seatsByRow.get(row);
            
            // Sort seats in the row by seat letter
            rowSeats.sort((s1, s2) -> {
                char letter1 = s1.getSeatNumber().replaceAll("[0-9]", "").charAt(0);
                char letter2 = s2.getSeatNumber().replaceAll("[0-9]", "").charAt(0);
                return letter1 - letter2;
            });
            
            // Find consecutive groups
            for (int i = 0; i <= rowSeats.size() - numberOfSeats; i++) {
                boolean consecutive = true;
                List<Seat> consecutiveGroup = new ArrayList<>();
                
                for (int j = 0; j < numberOfSeats - 1; j++) {
                    if (!areConsecutiveSeats(rowSeats.get(i + j), rowSeats.get(i + j + 1))) {
                        consecutive = false;
                        break;
                    }
                    consecutiveGroup.add(rowSeats.get(i + j));
                }
                
                if (consecutive) {
                    // Add the last seat
                    consecutiveGroup.add(rowSeats.get(i + numberOfSeats - 1));
                    return consecutiveGroup;
                }
            }
        }
        
        return new ArrayList<>();
    }

    public Seat saveSeat(Seat seat) {
        return seatRepository.save(seat);
    }

    public void deleteSeat(Long id) {
        seatRepository.deleteById(id);
    }
    
    public void generateRandomSeatsForFlight(Flight flight, int rows, int seatsPerRow) {
        Random random = new Random();
        String[] seatClasses = {"ECONOMY", "BUSINESS", "FIRST"};
        
        for (int row = 1; row <= rows; row++) {
            for (int seat = 0; seat < seatsPerRow; seat++) {
                char seatLetter = (char) ('A' + seat);
                String seatNumber = row + String.valueOf(seatLetter);
                
                Seat newSeat = new Seat();
                newSeat.setFlight(flight);
                newSeat.setSeatNumber(seatNumber);
                
                // Set seat properties
                newSeat.setWindow(seat == 0 || seat == seatsPerRow - 1);
                newSeat.setExitRow(row == 1 || row == rows || row == rows/2); // Exit rows at front, middle, back
                newSeat.setHasExtraLegroom(row == 1 || row == rows/2); // Extra legroom at front and exit rows
                
                // Randomly set if seat is occupied (30% chance)
                newSeat.setOccupied(random.nextDouble() < 0.3);
                
                // Set seat class based on row
                if (row <= rows * 0.1) { // First 10% of rows are first class
                    newSeat.setSeatClass("FIRST");
                } else if (row <= rows * 0.3) { // Next 20% are business class
                    newSeat.setSeatClass("BUSINESS");
                } else { // Rest are economy
                    newSeat.setSeatClass("ECONOMY");
                }
                
                seatRepository.save(newSeat);
            }
        }
    }
}