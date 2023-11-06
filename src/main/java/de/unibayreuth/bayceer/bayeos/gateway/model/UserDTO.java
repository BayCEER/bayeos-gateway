package de.unibayreuth.bayceer.bayeos.gateway.model;

public class UserDTO {
	
	long id;
	String name;
	String fullName;
	String domain;
	
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public UserDTO(User u) {
		this.id = u.getId();		
		this.name = u.getName();
		u.setFullName();
		this.fullName = u.getFullName();
		if (!u.inDefaultDomain()) {
			this.domain = u.getDomain().getName();
		}				
	}

	
	

	}