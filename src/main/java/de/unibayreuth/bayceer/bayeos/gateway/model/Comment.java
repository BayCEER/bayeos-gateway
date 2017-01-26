package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;

@Entity
public class Comment extends UniqueEntity {
	
	
	private static final long serialVersionUID = -251604491393646864L;


	@ManyToOne
	@JoinColumn(name="user_id",nullable=false)
	User user;
	
	
	@OrderBy("insertTime DESC")
	@Column(name="insert_time")	
	Date insertTime = new Date();
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
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getInsertTime() {
		return insertTime;
	}
	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}
	public Board getBoard() {
		return board;
	}
	public void setBoard(Board board) {
		this.board = board;
	}
	
	
	
	

    
}
