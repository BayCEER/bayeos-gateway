package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

public class BoardCommandDTO {
	
	
	public long id;
	public String origin;
	public Payload payload;
	public Short kind;
	public String description;
	public Payload response;
	public Short status;
	public Date insertTime;
	public Date responseTime;	
	
	
	public BoardCommandDTO() {
		
	}
	
	public BoardCommandDTO(BoardCommand c) {		
		this.id = c.getId();
		this.origin = c.getBoard().getOrigin();
		this.payload = c.getPayload();
		this.kind =  c.getKind();
		this.description = c.getDescription();
		this.response = c.getResponse();
		this.status = c.getStatus();
		this.insertTime = c.getInsertTime();
		this.responseTime = c.getResponseTime();
	}


	public BoardCommandDTO(long id, String origin, Payload payload, Short kind, String description, Payload response,
			Short status,Date insertTime, Date responseTime) {
		super();
		this.id = id;
		this.origin = origin;
		this.payload = payload;
		this.kind = kind;
		this.description = description;
		this.response = response;
		this.status = status;
		this.insertTime = insertTime;
		this.responseTime = responseTime;
	}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

   

    public Short getKind() {
        return kind;
    }

    public void setKind(Short kind) {
        this.kind = kind;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

   
    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public Payload getResponse() {
        return response;
    }

    public void setResponse(Payload response) {
        this.response = response;
    }


	
	
	
		
		
}
