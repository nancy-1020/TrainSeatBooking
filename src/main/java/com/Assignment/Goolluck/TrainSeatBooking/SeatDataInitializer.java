        package com.Assignment.Goolluck.TrainSeatBooking;

import com.Assignment.Goolluck.TrainSeatBooking.entity.Seat;
import com.Assignment.Goolluck.TrainSeatBooking.entity.SeatStatus;
import com.Assignment.Goolluck.TrainSeatBooking.repository.SeatRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeatDataInitializer {

    @Bean
    CommandLineRunner initializeSeats(SeatRepository seatRepository) {
        return args -> {
            if (seatRepository.count() == 0) {
                for (int i = 1; i <= 20; i++) {
                    Seat seat = new Seat();
                    seat.setSeatNumber(i);
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seatRepository.save(seat);
                }

                System.out.println("20 seats initialized successfully!");
            }
        };
    }
}
