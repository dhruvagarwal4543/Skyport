package com.skyport.app.models;

import java.io.Serializable;
import com.google.firebase.Timestamp;

public class Ticket implements Serializable {
    public String bookingId;
    public String from;
    public String to;
    public String fromCode;
    public String toCode;
    public Timestamp departureTime;
    public Timestamp arrivalTime;
    public String airline;
    public String flightNumber;
    public String gate;
    public String seat;
    public Timestamp createdAt;
    public String duration;
    public String passengerName;

    public Ticket() {
        // Required empty constructor for Firestore mapping
    }

    public Ticket(String bookingId, String from, String to, String fromCode, String toCode,
                  Timestamp departureTime, Timestamp arrivalTime, String airline,
                  String flightNumber, String gate, String seat, Timestamp createdAt,
                  String duration, String passengerName) {
        this.bookingId = bookingId;
        this.from = from;
        this.to = to;
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.airline = airline;
        this.flightNumber = flightNumber;
        this.gate = gate;
        this.seat = seat;
        this.createdAt = createdAt;
        this.duration = duration;
        this.passengerName = passengerName;
    }
}
