
package com.Assignment.Goolluck.TrainSeatBooking.service;

import com.Assignment.Goolluck.TrainSeatBooking.entity.Seat;
import com.Assignment.Goolluck.TrainSeatBooking.entity.SeatStatus;
import com.Assignment.Goolluck.TrainSeatBooking.exception.SeatNotAvailableException;
import com.Assignment.Goolluck.TrainSeatBooking.repository.SeatRepository;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    @Transactional
    public Seat lockSeat(Long id, String userId) {

        Seat seat = seatRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Seat not found"));

        Instant now = Instant.now();

        // Don't allow booking a seat that's already booked.
        if (seat.getStatus() == SeatStatus.BOOKED) {
            throw new SeatNotAvailableException("Seat is already booked");
        }

        // Another user may still be holding this seat.
        if (seat.getStatus() == SeatStatus.LOCKED
                && seat.getLockExpiresAt() != null
                && seat.getLockExpiresAt().isAfter(now)
                && !userId.equals(seat.getLockedBy())) {

            throw new SeatNotAvailableException(
                    "Seat is temporarily locked by another user");
        }

        // Hold the seat for two minutes.
        seat.setStatus(SeatStatus.LOCKED);
        seat.setLockedBy(userId);
        seat.setLockExpiresAt(now.plusSeconds(120));

        return seatRepository.save(seat);
    }

    @Transactional
    public Seat confirmBooking(Long id, String userId) {

        Seat seat = seatRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Seat not found"));

        Instant now = Instant.now();

        if (seat.getStatus() != SeatStatus.LOCKED
                || !userId.equals(seat.getLockedBy())) {
            throw new RuntimeException(
                    "You do not have a valid lock on this seat");
        }

        if (seat.getLockExpiresAt() == null
                || !seat.getLockExpiresAt().isAfter(now)) {

            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedBy(null);
            seat.setLockExpiresAt(null);
            seatRepository.save(seat);

            throw new RuntimeException("Seat lock has expired");
        }

        // Payment is simulated for this assignment.
        seat.setStatus(SeatStatus.BOOKED);
        seat.setLockedBy(null);
        seat.setLockExpiresAt(null);

        return seatRepository.save(seat);
    }

    @Transactional
    public Seat cancelLock(Long id, String userId) {

        Seat seat = seatRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Seat not found"));

        if (seat.getStatus() != SeatStatus.LOCKED
                || !userId.equals(seat.getLockedBy())) {
            throw new RuntimeException(
                    "You do not own the lock on this seat");
        }

        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setLockedBy(null);
        seat.setLockExpiresAt(null);

        return seatRepository.save(seat);
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void releaseExpiredLocks() {

        Instant now = Instant.now();

        List<Seat> expiredSeats =
                seatRepository.findExpiredSeatsForUpdate(
                        SeatStatus.LOCKED, now);

        for (Seat seat : expiredSeats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedBy(null);
            seat.setLockExpiresAt(null);
        }

        if (!expiredSeats.isEmpty()) {
            seatRepository.saveAll(expiredSeats);

            System.out.println(
                    "Released " + expiredSeats.size()
                            + " expired seat lock(s).");
        }
    }
}
