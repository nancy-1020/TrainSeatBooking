package com.Assignment.Goolluck.TrainSeatBooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrainSeatBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainSeatBookingApplication.class, args);
	}

}
