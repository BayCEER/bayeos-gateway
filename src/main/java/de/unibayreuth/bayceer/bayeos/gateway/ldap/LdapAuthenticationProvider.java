package de.unibayreuth.bayceer.bayeos.gateway.ldap;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.thymeleaf.util.StringUtils;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;

import de.unibayreuth.bayceer.bayeos.gateway.CustomUserDetails;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UserRepository;



public class LdapAuthenticationProvider implements AuthenticationProvider{
	
	
	private String dn;
	private String sn;
	private String givenName;
	
	private String host;	
	private int port;
	private Boolean ssl;
	private int version;
	private UserRepository userRepo;	
	private LDAPConnection con;	
	private Logger log = Logger.getLogger(LdapAuthenticationProvider.class);
	private Boolean refineUser;	
	private User user;
	

	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {			
		String[] context = StringUtils.split(auth.getName(), "@");        
    	if (context.length < 2) {
    		user = userRepo.findFirstByNameIgnoreCaseAndDomainIsNullAndLockedIsFalse(context[0]);
    	} else {
    		user = userRepo.findFirstByNameIgnoreCaseAndDomainNameIgnoreCaseAndLockedIsFalse(context[0],context[1]);
    	}    		               
        if (user == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        } 
							
        
		if (con == null) {
			if (ssl) {
				con = new LDAPConnection(new LDAPJSSESecureSocketFactory());
			} else {
				con = new LDAPConnection();
			}			
		}
		 
		UserDetails ud = new CustomUserDetails(user);
		try {
			con.connect(host, port);
			String dname  = String.format(dn,ud.getUsername());
			con.bind(version, dname, auth.getCredentials().toString().getBytes("UTF8"));			
			if (con.isBound()) {				
				if (refineUser) {
					if (user.getFirstName() == null || user.getLastName() == null) {
						LDAPEntry e = con.read(dname,new String[] {givenName,sn} );						
						user.setFirstName(e.getAttribute(givenName).getStringValue());
						user.setLastName(e.getAttribute(sn).getStringValue());
						userRepo.save(user);
						log.info("Refined user " + user.getName() + " with LDAP information");
					}
				}											
				UsernamePasswordAuthenticationToken a = new UsernamePasswordAuthenticationToken(ud, auth.getCredentials(),ud.getAuthorities()); 				
				a.setDetails(ud);
				user.setPassword(auth.getCredentials().toString());
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

		

	public void setUserRepo(UserRepository userRepo) {
		this.userRepo = userRepo;		
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public void setRefineUser(Boolean value) {
		this.refineUser = value;		
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	

	
	

}
