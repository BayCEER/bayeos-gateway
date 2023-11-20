package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

public class ChannelDTO extends UniqueEntity{
    
    private String nr;
    private String name;
    private String phenomena;
    private Date lastResultTime;
    private Float lastResultValue;    
    private String unit;
    
    public ChannelDTO(Channel channel) {    
        this.id = channel.getId();
        this.nr = channel.getNr();
        this.name = channel.getName();
        this.phenomena = channel.getPhenomena();
        this.lastResultTime = channel.getLastResultTime();
        this.lastResultValue = channel.getLastResultValue();
        if (channel.getUnit() != null) {
            unit = channel.getUnit().getName();
        }                
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhenomena() {
        return phenomena;
    }

    public void setPhenomena(String phenomena) {
        this.phenomena = phenomena;
    }

    public Date getLastResultTime() {
        return lastResultTime;
    }

    public void setLastResultTime(Date lastResultTime) {
        this.lastResultTime = lastResultTime;
    }

    public Float getLastResultValue() {
        return lastResultValue;
    }

    public void setLastResultValue(Float lastResultValue) {
        this.lastResultValue = lastResultValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

   
   
    
    
}
