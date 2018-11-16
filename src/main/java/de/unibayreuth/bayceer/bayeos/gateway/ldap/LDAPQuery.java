package de.unibayreuth.bayceer.bayeos.gateway.ldap;

public class LDAPQuery {
	public String givenName;
	public String cn;
	
	public LDAPQuery() {
		givenName = "*";
		cn = "*";
	}
	
	public LDAPQuery(String givenName, String cn) {
		this.givenName = givenName;
		this.cn = cn;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}
	
	@Override
	public String toString() {
		return "givenName:" + givenName + " cn:" + cn; 
	}

}
