package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class Comment extends UniqueEntity {
	
	
	private static final long serialVersionUID = -251604491393646864L;


	@ManyToOne
	@JoinColumn(name="user_id",nullable=false)
	@JsonView(DataTablesOutput.View.class)
	User user;
	
	
	@OrderBy("insertTime DESC")
	@Column(name="insert_time")	
	@JsonView(DataTablesOutput.View.class)
	Date insertTime = new Date();
	
	@JsonView(DataTablesOutput.View.class)
	String content;
	
	
	@ManyToOne()
	@JoinTable(name="board_comment",joinColumns=@JoinColumn(name="comment_id"),inverseJoinColumns=@JoinColumn(name="board_comments_id"))
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


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public Board getBoard() {
		return board;
	}


	public void setBoard(Board board) {
		this.board = board;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
	
	
	

    
}
