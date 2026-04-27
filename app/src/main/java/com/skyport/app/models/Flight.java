package com.skyport.app.models;

public class Flight {
    private String airline;       // code: "6E", "SG", etc.
    private String airline_name;  // friendly name from Firebase
    private String source;
    private String destination;
    private String departure_time;
    private String arrival_time;  // calculated or stored
    private String duration;
    private String price;
    private String flight_number;
    private String departure_date;
    private int    seats_available;
    private long   departureTimeMillis;
    private long   arrivalTimeMillis;

    public Flight(String airline, String airline_name, String source, String destination,
                  String departure_time, String arrival_time,
                  String duration, String price, String flight_number,
                  String departure_date, int seats_available,
                  long departureTimeMillis, long arrivalTimeMillis) {
        this.airline          = airline;
        this.airline_name     = airline_name;
        this.source           = source;
        this.destination      = destination;
        this.departure_time   = departure_time;
        this.arrival_time     = arrival_time;
        this.duration         = duration;
        this.price            = price;
        this.flight_number    = flight_number;
        this.departure_date   = departure_date;
        this.seats_available  = seats_available;
        this.departureTimeMillis = departureTimeMillis;
        this.arrivalTimeMillis   = arrivalTimeMillis;
    }

    public String getAirline()          { return airline; }
    public String getAirline_name()     { return airline_name; }
    public String getSource()           { return source; }
    public String getDestination()      { return destination; }
    public String getDeparture_time()   { return departure_time; }
    public String getArrival_time()     { return arrival_time; }
    public String getDuration()         { return duration; }
    public String getPrice()            { return price; }
    public String getFlight_number()    { return flight_number; }
    public String getDeparture_date()   { return departure_date; }
    public int    getSeats_available()  { return seats_available; }
    public long   getDepartureTimeMillis() { return departureTimeMillis; }
    public long   getArrivalTimeMillis()   { return arrivalTimeMillis; }
}