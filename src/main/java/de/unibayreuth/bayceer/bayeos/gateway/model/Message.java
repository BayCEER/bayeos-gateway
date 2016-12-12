package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
class Message extends UniqueEntity {
	

	String origin;		
	String type;
	String content;
	@Column(name="result_time")
	Date resultTime;
	
	@Column(name="insert_time")
	Date insertTime = new Date();
	
	public Message(String value){
		// Hotfix 1.9.12: replace all null chars with space
		this.content = value.replace((char)0x00,(char)0x20).trim();
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getResultTime() {
		return resultTime;
	}

	public void setResultTime(Date resultTime) {
		this.resultTime = resultTime;
	}

	public Date getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}
	
	

    
}
