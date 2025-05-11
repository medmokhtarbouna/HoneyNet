package edu.wustl.honeyrj.geoip;

public class GeoIPEntry {
    private final String ip;
    private final double latitude;
    private final double longitude;

    public GeoIPEntry(String ip, double latitude, double longitude) {
        this.ip = ip;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getIp() {
        return ip;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
