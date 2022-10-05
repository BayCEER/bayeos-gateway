package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.beans.PropertyEditorSupport;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class PayloadEditor extends PropertyEditorSupport {
    
    @Override
    public String getAsText() {        
        Payload p = (Payload)getValue();
        if (p != null) {
            String h = Hex.encodeHexString(p.getBytes(),true);
            StringBuffer b = new StringBuffer();            
            for (int i = 0;i<h.length();i++) {
                if (i%2==0) {
                    b.append(" ");
                }
                b.append(h.charAt(i));
            }                                                   
            return b.toString().stripLeading();                
        } else {
            return null;
        }
    }
    
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {        
        if (text == null || text.isEmpty()) {
            setValue(null);
        } else {
            Payload f = new Payload();
            try {
                f.setBytes(Hex.decodeHex(text.replace(" ", "")));
            } catch (DecoderException e) {
               throw new IllegalArgumentException("Invalid hex format");
            }
            setValue(f);

        }
    }
        
}


