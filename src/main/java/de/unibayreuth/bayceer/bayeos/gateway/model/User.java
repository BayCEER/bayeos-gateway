package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.AssertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;


@SuppressWarnings("deprecation")
@Entity
@Table(name="users")
public class User extends NamedDomainEntity {
			
	protected String password;
	
	@Column(name="first_name")
	private String firstName;
	
	@Column(name="last_name")
	private String lastName;	
	
	@Enumerated(EnumType.STRING)
	private Role role = Role.USER;;
	
	@Column(name="account_locked")
	private Boolean locked = false;
	
	@Transient
	private String newPassword;
		
	@Transient
	private String newPasswordRepetition;
	
	@OneToOne
	private Contact contact;
	
	@Transient 
	private String fullName;
				
		
	public void encodeNewPassword() {
		MessageDigestPasswordEncoder pwe = new MessageDigestPasswordEncoder("SHA");
		pwe.setEncodeHashAsBase64(true);
		this.password = pwe.encode(newPassword);						
	}
	
	@AssertTrue
	public boolean isPasswordMatch() {
		if (newPassword != null && newPasswordRepetition != null) {
			return newPassword.equals(newPasswordRepetition);
		} else {
			return newPassword == newPasswordRepetition;	
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
		this.contact  = user.getContact();
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
		

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}
	
	public boolean hasEmail() {
		return this.contact != null && this.contact.getEmail() != null;
	}
	
	
		
	public String getFullName() {
		return fullName;
	}

	public void setFullName() {	
		if (firstName == null && lastName == null) {
			this.fullName = name;
		} else {
			this.fullName = WordUtils.capitalizeFully(((firstName==null)?"":firstName) + ((lastName==null)?"":" " + lastName));	
		}			
	}
		
	
	
	
}
