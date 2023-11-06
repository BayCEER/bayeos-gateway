package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;

@Entity
public class InfluxConnection extends NamedDomainEntity{
    
    String url = "http://localhost:8086";
    String token;
    String bucket = "bayeos";        
    Boolean isDefault = false;
        
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getBucket() {
        return bucket;
    }
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
   
    public Boolean getIsDefault() {
        return isDefault;
    }
    public void setIsDefault(Boolean value) {
        this.isDefault = value;
    }    
}
