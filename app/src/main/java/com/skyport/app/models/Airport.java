package com.skyport.app.models;

public class Airport {
    private String city;
    private String iata;
    private String name;

    public Airport(String city, String iata, String name) {
        this.city = city;
        this.iata = iata;
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public String getIata() {
        return iata;
    }

    public String getName() {
        return name;
    }
}
