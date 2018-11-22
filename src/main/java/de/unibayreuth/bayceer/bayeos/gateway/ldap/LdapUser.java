package de.unibayreuth.bayceer.bayeos.gateway.ldap;

public class LdapUser {
	public String firstName;
	public String lastName;
	public String name;

	public LdapUser() {
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}