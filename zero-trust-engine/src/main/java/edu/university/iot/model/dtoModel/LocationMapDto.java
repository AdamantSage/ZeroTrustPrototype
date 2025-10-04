package edu.university.iot.model.dtoModel;

public class LocationMapDto {
    private String id;
    private String displayName;
    private double latitude;
    private double longitude;
    private String type; // ACADEMIC, LAB, SOCIAL, RESTRICTED, EXTERNAL
    private String subnet;

    // Constructors
    public LocationMapDto() {}

    public LocationMapDto(String id, String displayName, double latitude, double longitude, String type, String subnet) {
        this.id = id;
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.subnet = subnet;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubnet() { return subnet; }
    public void setSubnet(String subnet) { this.subnet = subnet; }
}