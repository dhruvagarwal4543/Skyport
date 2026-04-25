package com.skyport.app.models;

public class Flight {
    private String airline;
    private String source;
    private String destination;
    private String departure_time;
    private String arrival_time;
    private String duration;
    private String price;
    private String flight_number;

    public Flight(String airline, String source, String destination,
                  String departure_time, String arrival_time,
                  String duration, String price, String flight_number) {
        this.airline        = airline;
        this.source         = source;
        this.destination    = destination;
        this.departure_time = departure_time;
        this.arrival_time   = arrival_time;
        this.duration       = duration;
        this.price          = price;
        this.flight_number  = flight_number;
    }

    public String getAirline()        { return airline; }
    public String getSource()         { return source; }
    public String getDestination()    { return destination; }
    public String getDeparture_time() { return departure_time; }
    public String getArrival_time()   { return arrival_time; }
    public String getDuration()       { return duration; }
    public String getPrice()          { return price; }
    public String getFlight_number()  { return flight_number; }
}