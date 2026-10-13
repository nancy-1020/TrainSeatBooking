
package com.Assignment.Goolluck.TrainSeatBooking.repository;

import com.Assignment.Goolluck.TrainSeatBooking.entity.Seat;
import com.Assignment.Goolluck.TrainSeatBooking.entity.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT s FROM Seat s
    WHERE s.status = :status
      AND s.lockExpiresAt <= :now
""")
    List<Seat> findExpiredSeatsForUpdate(
            @Param("status") SeatStatus status,
            @Param("now") Instant now
    );

}
