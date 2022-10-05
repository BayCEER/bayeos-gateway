package de.unibayreuth.bayceer.bayeos.gateway.event;

public class NewCommandResponseEvent extends Event {
	private Short key;
	private Short status;
	private byte[] payload;
	
	public NewCommandResponseEvent(Long id,Short key, Short status, byte[] payload) {
		super(id,EventType.NEW_COMMAND_RESPONSE);
		this.key = key;
		this.status = status;
		this.payload = payload;
	}

	
	

	public Short getKey() {
		return key;
	}


	public void setKey(Short key) {
		this.key = key;
	}


	public Short getStatus() {
		return status;
	}


	public void setStatus(Short status) {
		this.status = status;
	}




    public byte[] getPayload() {
        return payload;
    }




    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
	
	

}
