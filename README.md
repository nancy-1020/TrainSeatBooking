# SeatLock 

SeatLock is a train seat booking application I built using Java Spring Boot, React, and MySQL. The main goal was to handle seat bookings safely, especially when multiple users try to book the same seat.

### How I Built It

Built REST APIs using Spring Boot to manage seats, locking, booking confirmation, and cancellation.
Used MySQL with Spring Data JPA to store seat details and booking status.
Implemented temporary seat locks for 2 minutes, with a countdown timer on the frontend.
Added automatic release of expired locks so seats become available again.
Used database-level locking to prevent multiple users from booking the same seat simultaneously.
Built the frontend using React and connected it to the backend APIs.
Added a simulated payment step to demonstrate the booking flow.
Created a Docker image for the backend.

### Architecture
<img width="1672" height="941" alt="Architecture" src="https://github.com/user-attachments/assets/dcadbd7a-4e2a-415f-9fc2-c97f6b49fbcc" />


### Tech Stack

Java · Spring Boot · Spring Data JPA · MySQL · React · Vite · Docker

### Screenshots
1.<img width="1920" height="1080" alt="Screenshot (259)" src="https://github.com/user-attachments/assets/498395ce-1491-48b1-bf96-774c7b5c4aef" />

2.I also tested the backend using Apache JMeter with 100 requests to see how it performs under load. This helped me check the API response time and identify issues when handling multiple requests.
<img width="1920" height="1080" alt="Screenshot (252)" src="https://github.com/user-attachments/assets/c7076725-54e2-44d5-9e49-26d61c128803" />


3.I tested what happens when multiple users try to book the same seat at the same time using Apache JMeter. Only one request can successfully lock the seat, while the others are rejected . This helps prevent duplicate bookings.

<img width="1920" height="1080" alt="Screenshot (253)" src="https://github.com/user-attachments/assets/cd02df3f-4f07-42d2-b904-d1632997590d" />

4.I also tested the API by trying to lock a seat that doesn't exist. The backend returned a `404 Not Found` response with a clear error message instead of processing an invalid request.

<img width="1920" height="1080" alt="Screenshot (258)" src="https://github.com/user-attachments/assets/38c6d69a-1497-4639-95e2-89fb4a273415" />



### Running Locally

1. Configure MySQL and the database settings.
2. Start the Spring Boot backend on port `8080`.
3. Open the frontend folder and run `npm install`, followed by `npm run dev`.
