package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.ArrayList;
import java.util.List;

public class BoardDTO extends NamedDomainEntity{
    
    public List<ChannelDTO> channels;
    private String origin;
    private Integer dbFolderId;
    
        
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
}
