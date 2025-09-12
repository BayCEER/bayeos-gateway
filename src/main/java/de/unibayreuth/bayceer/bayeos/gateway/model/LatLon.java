package de.unibayreuth.bayceer.bayeos.gateway.model;

public class LatLon {
    
    public Float Lat;
    public Float Lon;
    
    
    public LatLon(Float lat, Float lng) {
        super();
        Lat = lat;
        Lon = lng;
    }
    
    
    public Float getLat() {
        return Lat;
    }
    public void setLat(Float lat) {
        Lat = lat;
    }
    public Float getLon() {
        return Lon;
    }
    public void setLon(Float lon) {
        Lon = lon;
    }
    
}
