package com.skyport.app.models;

public class Flight {
    private String airlineName;
    private String route;
    private String time;

    public Flight(String airlineName, String route, String time) {
        this.airlineName = airlineName;
        this.route = route;
        this.time = time;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public String getRoute() {
        return route;
    }

    public String getTime() {
        return time;
    }
}
