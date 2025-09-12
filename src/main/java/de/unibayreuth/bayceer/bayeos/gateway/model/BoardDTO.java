package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.ArrayList;
import java.util.List;

public class BoardDTO extends NamedDomainEntity{
    
    public List<ChannelDTO> channels;
    private String origin;
    private Integer dbFolderId;
    private Float Lon;
    private Float Lat;
    private Float Alt;
    
        
    public BoardDTO(Board c) {        
       this.id = c.id;          
       this.name = c.name;
       this.setOrigin(c.origin);        
       this.channels = new ArrayList<ChannelDTO>();              
       if (c.channels!=null) {           
           for (Channel channel : c.channels) {
               channels.add(new ChannelDTO(channel));
           }           
       }
       this.dbFolderId = c.dbFolderId;
       this.Lon=c.getLon();
       this.Lat=c.getLat();
       this.Alt = c.getAlt();
    }


    public String getOrigin() {
        return origin;
    }


    public void setOrigin(String origin) {
        this.origin = origin;
    }


    public List<ChannelDTO> getChannels() {
        return channels;
    }


    public void setChannels(List<ChannelDTO> channels) {
        this.channels = channels;
    }


    public Integer getDbFolderId() {
        return dbFolderId;
    }


    public void setDbFolderId(Integer dbFolderId) {
        this.dbFolderId = dbFolderId;
    }


    public Float getLon() {
        return Lon;
    }


    public void setLon(Float lon) {
        Lon = lon;
    }


    public Float getLat() {
        return Lat;
    }


    public void setLat(Float lat) {
        Lat = lat;
    }


    public Float getAlt() {
        return Alt;
    }


    public void setAlt(Float alt) {
        Alt = alt;
    }
}
