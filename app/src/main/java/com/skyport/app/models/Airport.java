package com.skyport.app.models;

public class Airport {
    private String city;
    private String iata;
    private String name;
    private double latitude;
    private double longitude;
    private boolean hasCoordinates;

    /** Constructor without coordinates (backwards-compatible) */
    public Airport(String city, String iata, String name) {
        this.city           = city;
        this.iata           = iata;
        this.name           = name;
        this.hasCoordinates = false;
    }

    /** Constructor with coordinates */
    public Airport(String city, String iata, String name, double latitude, double longitude) {
        this.city           = city;
        this.iata           = iata;
        this.name           = name;
        this.latitude       = latitude;
        this.longitude      = longitude;
        this.hasCoordinates = true;
    }

    public String getCity()         { return city; }
    public String getIata()         { return iata; }
    public String getName()         { return name; }
    public double getLatitude()     { return latitude; }
    public double getLongitude()    { return longitude; }
    public boolean hasCoordinates() { return hasCoordinates; }
}
