package de.unibayreuth.bayceer.bayeos.gateway;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;



public class LdapAuthenticationProvider implements AuthenticationProvider{
				
	private String dn;
	private String host;	
	private int port;
	private Boolean ssl;
	private int version;	
	private UserDetailsService userDetailsService;
	private LDAPConnection con;
	
	private Logger log = Logger.getLogger(LdapAuthenticationProvider.class);

	
	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {		
		UserDetails u = userDetailsService.loadUserByUsername(auth.getName());						
		if (con == null) {
			if (ssl) {
				con = new LDAPConnection(new LDAPJSSESecureSocketFactory());
			} else {
				con = new LDAPConnection();
			}			
		}
		
		try {
			con.connect(host, port);			
			con.bind(version, String.format(dn,u.getUsername()), auth.getCredentials().toString().getBytes("UTF8"));			
			if (con.isBound()) {
				UsernamePasswordAuthenticationToken a = new UsernamePasswordAuthenticationToken(u, auth.getCredentials(),u.getAuthorities()); 				
				a.setDetails(u);
				return a;
			} else {
				return null;			
			}			
		} catch (LDAPException | UnsupportedEncodingException e) {
			log.warn(e.getMessage());
			return null;
		} finally {
			try {
				if (con != null && con.isConnected()) {
					con.disconnect();	
				}				
			} catch (LDAPException e) {
				log.error(e.getMessage());
			}
		}						
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);

	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Boolean getSsl() {
		return ssl;
	}

	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public LDAPConnection getCon() {
		return con;
	}

	public void setCon(LDAPConnection con) {
		this.con = con;
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}
	

}
