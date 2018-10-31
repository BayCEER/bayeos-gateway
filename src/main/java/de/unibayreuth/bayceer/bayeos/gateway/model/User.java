package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.AssertTrue;

import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

@Entity
@Table(name="users")
public class User extends NamedDomainEntity {
			
	protected String password;
	
	@Enumerated(EnumType.STRING)
	private Role role = Role.USER;;
	
	@Column(name="account_locked")
	private Boolean locked = false;
	
	@Transient
	private String newPassword;
		
	@Transient
	private String newPasswordRepetition;
	
	public boolean newPassword() {
		return (newPassword != null) && (newPasswordRepetition != null); 
	}
	
	public void encodeNewPassword() {
		MessageDigestPasswordEncoder pwe = new ShaPasswordEncoder();
		pwe.setEncodeHashAsBase64(true);
		this.password = pwe.encodePassword(newPassword,null);						
	}
	
	@AssertTrue
	public boolean isPasswordMatch() {
		if (newPassword()) {			
			return newPassword.equals(newPasswordRepetition);
		} else {
			return true;
		}
	}
		
	public boolean isUser() {
		return role.equals(Role.USER);
	}
				
	
	public boolean inNullDomain() {
		return this.domain == null;
	}
	
	public Long getDomainId() {
		return (domain == null)?null:domain.getId();
	}

	public User() {
		super();
	}

	public User(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.password = user.getPassword();
		this.role = user.getRole();
		this.locked = user.getLocked();	
		this.domain = user.getDomain();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewPasswordRepetition() {
		return newPasswordRepetition;
	}

	public void setNewPasswordRepetition(String newPasswordRepetition) {
		this.newPasswordRepetition = newPasswordRepetition;
	}
	
	public String getUserName() {
		return name;
	}
	
	public void setUserName(String name) {
		this.name = name;
	}
	
	
	
	
}
