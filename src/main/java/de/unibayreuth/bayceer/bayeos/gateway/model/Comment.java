package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Comment extends UniqueEntity {	
	
	@ManyToOne
	@JoinColumn(name="user_id",nullable=false)
	User user;
	Date insert_time = new Date();
	String content;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Date getInsertTime() {
		return insert_time;
	}
	public void setInsertTime(Date insert_time) {
		this.insert_time = insert_time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
	
	

    
}
