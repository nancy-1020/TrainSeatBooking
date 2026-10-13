
package com.Assignment.Goolluck.TrainSeatBooking.controller;

import com.Assignment.Goolluck.TrainSeatBooking.entity.Seat;
import com.Assignment.Goolluck.TrainSeatBooking.service.SeatService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "http://localhost:5173")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public List<Seat> getAllSeats() {
        return seatService.getAllSeats();
    }

    @PostMapping("/{id}/lock")
    public Seat lockSeat(
            @PathVariable Long id,
            @RequestParam String userId) {

        return seatService.lockSeat(id, userId);
    }

    @PostMapping("/{id}/confirm")
    public Seat confirmBooking(
            @PathVariable Long id,
            @RequestParam String userId) {

        return seatService.confirmBooking(id, userId);
    }

    @DeleteMapping("/{id}/lock")
    public Seat cancelLock(
            @PathVariable Long id,
            @RequestParam String userId) {

        return seatService.cancelLock(id, userId);
    }
}
