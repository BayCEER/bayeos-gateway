package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class BoardCommand extends UniqueEntity{
	
	
	
	public BoardCommand() {
		
	}
	
	

	public BoardCommand(User user, Date insertTime, 
			Date responseTime, Short kind, Payload payload, 
			String description, Short status, 
			Payload response, 
			Board board) {
		super();
		this.user = user;
		this.insertTime = insertTime;
		this.responseTime = responseTime;
		this.status = status;
		this.payload = payload;
		this.description = description;		
		this.board = board;
		this.kind = kind;
				
	}
	
	

	@ManyToOne
	@JoinColumn(name="user_id",nullable=false)
	@JsonView(DataTablesOutput.View.class)
	User user;
	
	@OrderBy("insertTime DESC")
	@Column(name="insert_time")	
	@JsonView(DataTablesOutput.View.class)
	Date insertTime = new Date();
		
	@Column(name="response_time")	
	@JsonView(DataTablesOutput.View.class)
	Date responseTime;
	
	 
	@AttributeOverride(name="payload",column= @Column(name="payload"))
	Payload payload;
	
	@AttributeOverride(name="payload",column= @Column(name="response"))
	Payload response;
	    
	
	@JsonView(DataTablesOutput.View.class)
	String description;
	
	@JsonView(DataTablesOutput.View.class)
	Short kind;
	
	@JsonView(DataTablesOutput.View.class)
	Short status;

		
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="board_id", nullable = false)
	Board board;
	
	

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	
	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Short getKind() {
		return kind;
	}

	public void setKind(Short kind) {
		this.kind = kind;
	}
	


    public Short getStatus() {
        return status;
    }



    public void setStatus(Short status) {
        this.status = status;
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
