package com.skyport.app.models;

import java.io.Serializable;

/**
 * Static ticket model — Firebase fields can be added later by mapping
 * Firestore document snapshots to this same class.
 */
public class Ticket implements Serializable {
    // Route
    public String fromCity;       // e.g. "Delhi (DEL)"
    public String fromTime;       // e.g. "18:55 Tue, 17 Mar"
    public String fromTerminal;   // e.g. "Terminal 3"
    public String toCity;
    public String toTime;
    public String toTerminal;

    // Detail fields
    public String gate;           // e.g. "C2"
    public String flight;         // e.g. "KL-838"
    public String seat;           // e.g. "14E"
    public String traveller;      // e.g. "Nikhil Kumar"
    public String travelClass;    // e.g. "Economy"
    public String ticketNumber;   // e.g. "01672721252926"

    public Ticket(String fromCity, String fromTime, String fromTerminal,
                  String toCity,   String toTime,   String toTerminal,
                  String gate,     String flight,   String seat,
                  String traveller, String travelClass, String ticketNumber) {
        this.fromCity     = fromCity;
        this.fromTime     = fromTime;
        this.fromTerminal = fromTerminal;
        this.toCity       = toCity;
        this.toTime       = toTime;
        this.toTerminal   = toTerminal;
        this.gate         = gate;
        this.flight       = flight;
        this.seat         = seat;
        this.traveller    = traveller;
        this.travelClass  = travelClass;
        this.ticketNumber = ticketNumber;
    }

    // ── Static seed data ───────────────────────────────────────────────────────
    public static java.util.List<Ticket> getUpcomingTickets() {
        java.util.List<Ticket> list = new java.util.ArrayList<>();
        list.add(new Ticket(
                "Delhi (DEL)",   "18:55 Tue, 17 Mar", "Terminal 3",
                "Mumbai (BOM)",  "21:40 Tue, 17 Mar", "Terminal 2",
                "C2", "KL-838", "14E",
                "Nikhil Kumar", "Economy", "01672721252926"
        ));
        list.add(new Ticket(
                "Bangalore (BLR)", "06:30 Wed, 25 Apr", "Terminal 1",
                "Chennai (MAA)",   "07:45 Wed, 25 Apr", "Terminal 4",
                "A5", "6E-302", "22A",
                "Nikhil Kumar", "Economy", "01672721253142"
        ));
        return list;
    }
}
