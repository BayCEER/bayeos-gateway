package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class Message extends DomainEntity {
	
	
	
	public Message() {
		super();		
	}

	@JsonView(DataTablesOutput.View.class)
	String origin;		
	
	@JsonView(DataTablesOutput.View.class)
	String type;
	
	
	@JsonView(DataTablesOutput.View.class)
	String content;
	
	@JsonView(DataTablesOutput.View.class)
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
