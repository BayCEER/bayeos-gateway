package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

public class BoardCommandDTO {
	
	public BoardCommandDTO() {
		
	}
	
	public BoardCommandDTO(BoardCommand cmd) {
		this.id = cmd.id;
		this.origin = cmd.board.origin;
		this.value = cmd.value;
		this.response = cmd.response;
		this.insertTime = cmd.insertTime;
		this.responseTime = cmd.responseTime;
		
	
	}

	public BoardCommandDTO(long id, String origin, String value, String response, Date insertTime, Date responseTime) {
		super();
		this.id = id;
		this.origin = origin;
		this.value = value;
		this.response = response;
		this.insertTime = insertTime;
		this.responseTime = responseTime;
	}
	public long id;
	public String origin;
	public String value;
	public String response;
	public Date insertTime;
	public Date responseTime;	
	
	
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
	
	}
