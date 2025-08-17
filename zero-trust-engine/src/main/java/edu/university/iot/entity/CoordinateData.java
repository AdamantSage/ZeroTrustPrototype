package edu.university.iot.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class CoordinateData {
    private double lat;
    private double lng;
    private String subnet;

    // === Constructors ===
    public CoordinateData() {}
    public CoordinateData(double lat, double lng, String subnet) {
        this.lat = lat;
        this.lng = lng;
        this.subnet = subnet;
    }

    // === Getters and Setters ===
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public String getSubnet() { return subnet; }
    public void setSubnet(String subnet) { this.subnet = subnet; }
}
