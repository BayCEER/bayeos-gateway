package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Upload extends NamedDomainEntity {
	
	private UUID uuid;
	private Date uploadTime;
	private Date importTime;
	private String importMessage;
	private long size;
	
	@ManyToOne()
	@JoinColumn(name="user_id")
	private User user;

		
	public Upload() {
		super();
	}

	
	
	public String extension() {
		if (this.name.contains(".")) {
			String ext = this.name.substring(this.name.lastIndexOf(".")+1); 
			return ext.toLowerCase();
		} else {
			return "";
		}
	}



	public UUID getUuid() {
		return uuid;
	}



	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}



	public Date getUploadTime() {
		return uploadTime;
	}



	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}



	public Date getImportTime() {
		return importTime;
	}



	public void setImportTime(Date importTime) {
		this.importTime = importTime;
	}



	public String getImportMessage() {
		return importMessage;
	}



	public void setImportMessage(String importMessage) {
		this.importMessage = importMessage;
	}



	public long getSize() {
		return size;
	}



	public void setSize(long size) {
		this.size = size;
	}



	public User getUser() {
		return user;
	}



	public void setUser(User user) {
		this.user = user;
	}
}
