package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;


@Entity
public class Upload extends NamedDomainEntity {
	
	private static final long kB = 1024;
	private static final long MB = kB * 1024;
	private static final long GB = MB * 1024;
		
	UUID uuid;
	
	Date uploadTime;
    
    Date importTime;
    
	String importMessage;
	long size;
		
	@ManyToOne()
	@JoinColumn(name="user_id")
	User user;

	@Transient
	String sizeAsString;
	
	public void setSizeAsString() {
		StringBuffer s = new StringBuffer();
		if (size < kB) {
			s.append(String.valueOf(size)).append(" Bytes");
		} else if (size < MB ) {
			s.append(String.valueOf(Math.round((float)size/kB))).append(" kB");
		} else if (size < GB) {
			s.append(String.valueOf(Math.round((float)size/MB))).append(" MB");
		} else {
			s.append(String.valueOf(Math.round((float)size/GB))).append(" GB");
		}
		sizeAsString = s.toString();
	}
		
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

	public String getSizeAsString() {
		return sizeAsString;
	}
}
