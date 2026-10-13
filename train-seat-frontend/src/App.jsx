import { useState, useEffect, useCallback } from "react";
import "./App.css";

const API_URL = "http://localhost:8080/api/seats";

// Demo identity only; this is not real authentication.
const USER_ID = "demo-user-1";

function App() {
  const [seats, setSeats] = useState([]);
  const [selectedSeatId, setSelectedSeatId] = useState(null);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [now, setNow] = useState(Date.now());

  const loadSeats = useCallback(async () => {
    try {
      const response = await fetch(API_URL);

      if (!response.ok) {
        throw new Error("Could not load seats.");
      }

      const data = await response.json();
      setSeats(data);
    } catch (error) {
      setMessage(
        "Cannot connect to the server. Make sure Spring Boot is running."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSeats();

    // Refresh seat availability periodically.
    const refreshInterval = setInterval(loadSeats, 3000);
    const clockInterval = setInterval(() => setNow(Date.now()), 1000);

    return () => {
      clearInterval(refreshInterval);
      clearInterval(clockInterval);
    };
  }, [loadSeats]);

  const available = seats.filter(
    (seat) => seat.status === "AVAILABLE"
  ).length;

  const locked = seats.filter(
    (seat) => seat.status === "LOCKED"
  ).length;

  const booked = seats.filter(
    (seat) => seat.status === "BOOKED"
  ).length;

  const selected = seats.find(
    (seat) => seat.id === selectedSeatId
  );

  const remainingSeconds = selected?.lockExpiresAt
    ? Math.max(
        0,
        Math.ceil(
          (new Date(selected.lockExpiresAt).getTime() - now) / 1000
        )
      )
    : 0;

  const countdown = `${Math.floor(remainingSeconds / 60)
    .toString()
    .padStart(2, "0")}:${(remainingSeconds % 60)
    .toString()
    .padStart(2, "0")}`;

  async function performAction(url, method, successMessage) {
    setBusy(true);
    setMessage("");

    try {
      const response = await fetch(url, {
        method,
      });

      const responseText = await response.text();
      let result = {};

      try {
        result = responseText ? JSON.parse(responseText) : {};
      } catch {
        result = {};
      }

      if (!response.ok) {
        throw new Error(
          result.message ||
            result.error ||
            responseText ||
            "Request failed. Please try again."
        );
      }

      setMessage(successMessage);

      // Get the latest state from the database.
      await loadSeats();

      return true;
    } catch (error) {
      setMessage(error.message || "Something went wrong.");
      await loadSeats();
      return false;
    } finally {
      setBusy(false);
    }
  }

  function selectSeat(seat) {
    if (seat.status !== "AVAILABLE") {
      setMessage(`Seat ${seat.seatNumber} is ${seat.status.toLowerCase()}.`);
      return;
    }

    setSelectedSeatId(seat.id);
    setMessage("");
  }

  async function lockSeat() {
    if (!selected) return;

    const success = await performAction(
      `${API_URL}/${selected.id}/lock?userId=${encodeURIComponent(USER_ID)}`,
      "POST",
      `Seat ${selected.seatNumber} locked for checkout.`
    );

    if (success) {
      setSelectedSeatId(selected.id);
    }
  }

  async function confirmBooking() {
    if (!selected) return;

    const success = await performAction(
      `${API_URL}/${selected.id}/confirm?userId=${encodeURIComponent(USER_ID)}`,
      "POST",
      `Seat ${selected.seatNumber} booked successfully!`
    );

    if (success) {
      setSelectedSeatId(null);
    }
  }

  async function cancelReservation() {
    if (!selected) return;

    const success = await performAction(
      `${API_URL}/${selected.id}/lock?userId=${encodeURIComponent(USER_ID)}`,
      "DELETE",
      "Reservation cancelled."
    );

    if (success) {
      setSelectedSeatId(null);
    }
  }

  return (
    <main className="app">
      <header className="header">
        <div className="brand-icon">🚆</div>
        <div>
          <h1>Train Seat Booking</h1>
          <p>Train 101 · Coach A</p>
        </div>
        <span className="seat-total">{seats.length} Seats</span>
      </header>

      <section className="summary">
        <div className="summary-card available">
          <span>Available</span>
          <strong>{available}</strong>
        </div>
        <div className="summary-card locked">
          <span>Locked</span>
          <strong>{locked}</strong>
        </div>
        <div className="summary-card booked">
          <span>Booked</span>
          <strong>{booked}</strong>
        </div>
      </section>

      <section className="content">
        <div className="coach">
          <div className="section-heading">
            <div>
              <h2>Coach A</h2>
              <p>Select an available seat to continue</p>
            </div>
          </div>

          <div className="legend">
            <span><i className="dot green" /> Available</span>
            <span><i className="dot yellow" /> Locked</span>
            <span><i className="dot red" /> Booked</span>
          </div>

          {loading ? (
            <p>Loading seats...</p>
          ) : (
            <div className="seat-grid">
              {seats.map((seat) => (
                <button
                  key={seat.id}
                  className={`seat ${seat.status.toLowerCase()} ${
  selectedSeatId === seat.id ? "selected" : ""
}`}
                  onClick={() => selectSeat(seat)}
                  disabled={busy}
                  aria-label={`Seat ${seat.seatNumber}, ${seat.status}`}
                >
                  <span>{seat.seatNumber}</span>
                  <small>
                    {seat.status === "AVAILABLE"
                      ? "Free"
                      : seat.status === "LOCKED"
                      ? "Locked"
                      : "Booked"}
                  </small>
                </button>
              ))}
            </div>
          )}

          <p className="coach-footer">
            {seats.length} seats · One coach · Train 101
          </p>
        </div>

        <aside className="booking-panel">
          <h2>Reservation</h2>
          <p className="panel-subtitle">Your selected seat details</p>

          {selected ? (
            <>
              <div className="selected-seat">
                <span>Selected seat</span>
                <strong>Seat {selected.seatNumber}</strong>
                <span className="status-pill">{selected.status}</span>
              </div>

              {selected.status === "AVAILABLE" && (
                <>
                  <p className="booking-info">
                    Lock this seat to begin your reservation.
                  </p>
                  <button
                    className="primary-btn"
                    onClick={lockSeat}
                    disabled={busy}
                  >
                    {busy ? "Please wait..." : "Lock Seat & Checkout"}
                  </button>
                </>
              )}

              {selected.status === "LOCKED" &&
                selected.lockedBy === USER_ID && (
                  <>
                    <p className="booking-info">
                      Your seat is temporarily reserved.
                    </p>
                    <h3>Time remaining: {countdown}</h3>

                    {remainingSeconds > 0 ? (
                      <div className="action-buttons">
                        <button
                          className="confirm-btn"
                          onClick={confirmBooking}
                          disabled={busy}
                        >
                          Confirm Booking
                        </button>
                        <button
                          className="cancel-btn"
                          onClick={cancelReservation}
                          disabled={busy}
                        >
                          Cancel Reservation
                        </button>
                      </div>
                    ) : (
                      <p>Your lock has expired. Refreshing seat status...</p>
                    )}
                  </>
                )}

              {selected.status === "LOCKED" &&
                selected.lockedBy !== USER_ID && (
                  <p className="booking-info">
                    This seat is temporarily locked by another user.
                  </p>
                )}

              {selected.status === "BOOKED" && (
                <p className="booking-info">
                  This seat has already been booked.
                </p>
              )}
            </>
          ) : (
            <div className="empty-state">
              <div className="empty-icon">▦</div>
              <h3>No seat selected</h3>
              <p>Choose a green seat from the coach layout to get started.</p>
            </div>
          )}

          {message && <div className="message">{message}</div>}

          <div className="demo-note">
            <strong>Booking process</strong>
            <p>Select a seat → Lock → Confirm or Cancel</p>
          </div>
        </aside>
      </section>

      <footer className="footer">
        SeatLock · Train 101 · Demo application
      </footer>
    </main>
  );
}

export default App;
