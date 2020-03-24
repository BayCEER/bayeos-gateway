package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;

@Entity
public class Upload extends NamedDomainEntity {
	
	private UUID uuid;
	private Date uploadTime;
	private long size;
	private String userName;
		
	public Upload() {
		super();
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}
	
	public String extension() {
		if (this.name.contains(".")) {
			String ext = this.name.substring(this.name.lastIndexOf(".")+1); 
			return ext.toLowerCase();
		} else {
			return "";
		}
	}
}
