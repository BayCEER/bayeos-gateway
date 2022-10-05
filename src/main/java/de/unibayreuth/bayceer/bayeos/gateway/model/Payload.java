package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Embeddable;

@Embeddable
public class Payload {
    private byte[] payload;
    
    public Payload() {
     
    }
    
    
    public Payload(byte[] payload) {
        super();
        this.payload = payload;
    }


    public byte[] getBytes() {
        return payload;
    }

    public void setBytes(byte[] payload) {       
        this.payload = payload;
    }

    
}
