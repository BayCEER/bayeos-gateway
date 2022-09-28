package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

public class BoardCommandDTO {
	
	
	public long id;
	public String origin;
	public String value;
	public Short kind;
	public String description;
	public String response;
	public Short responseStatus;
	public Date insertTime;
	public Date responseTime;	
	
	
	public BoardCommandDTO() {
		
	}
	
	public BoardCommandDTO(BoardCommand c) {		
		this.id = c.getId();
		this.origin = c.getBoard().getOrigin();
		this.value = c.getValue();
		this.kind =  c.getKind();
		this.description = c.getDescription();
		this.response = c.getResponse();
		this.responseStatus = c.getResponseStatus();
		this.insertTime = c.getInsertTime();
		this.responseTime = c.getResponseTime();
	}


	public BoardCommandDTO(long id, String origin, String value, Short kind, String description, String response,
			Short responseStatus,Date insertTime, Date responseTime) {
		super();
		this.id = id;
		this.origin = origin;
		this.value = value;
		this.kind = kind;
		this.description = description;
		this.response = response;
		this.responseStatus = responseStatus;
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


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
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


	public String getResponse() {
		return response;
	}


	public void setResponse(String response) {
		this.response = response;
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

	public Short getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(Short responseStatus) {
		this.responseStatus = responseStatus;
	}
	
	
	
		
		
}
