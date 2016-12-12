package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name="users")
public class User extends UniqueEntity {
	
	@Column(name="username")	
	private String userName;

	@NotEmpty(message = "Password is required.")	
	private String password;

	@Enumerated(EnumType.STRING)
	private Role role;
	
	
	@Column(name="account_locked")
	private Boolean locked = false;
	
	public User() {

	}

	

	public Boolean getLocked() {
		return locked;
	}


	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public User(User user) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.password = user.getPassword();
		this.role = user.getRole();
		this.locked = user.getLocked();
	}

		
	public String getPassword() {
		return password;
	}

	public Role getRole() {
		return role;
	}
	
	public String getUserName() {
		return userName;
	}

	public boolean isAdmin(){
		return role == Role.ADMIN;
	}

	@Override
	public String toString() {
		return "User[" + id + "]: UserName[" + userName + "] Role[" + role + "]";
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
	
//	@PrePersist
//	public void hashPassword(){
//		MessageDigestPasswordEncoder pwe = new ShaPasswordEncoder();
//		pwe.setEncodeHashAsBase64(true);		
//		this.password = pwe.encodePassword(this.password,null );
//	}
	
	
	
}
