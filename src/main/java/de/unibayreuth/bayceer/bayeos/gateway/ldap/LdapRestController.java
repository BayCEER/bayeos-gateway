package de.unibayreuth.bayceer.bayeos.gateway.ldap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchResults;

@RestController
public class LdapRestController {		
	
	@Value("${LDAP_BASE:ou=users}")
	private String ldap_base;	
		
	@Autowired
	LdapAuthenticationProvider authProv;
	
	private Logger log = LoggerFactory.getLogger(LdapRestController.class);
		    	
	@RequestMapping(path="/rest/ldap/search", method = RequestMethod.GET)
	public List<Map<String,String>> search(String filter){
		return search(filter,new String[] {"cn", authProv.getGivenName(),authProv.getSn()});		
	}
	
	@RequestMapping(path="/rest/ldap/searchUser", method = RequestMethod.GET)
	public List<Map<String,String>> searchUser(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String userName) {		
		return search(String.format("(&(%s=%s)(%s=%s)(%s=%s))","cn",userName,authProv.getGivenName(),firstName,authProv.getSn(),lastName));		
	}
	
	private List<Map<String,String>> search(String filter, String[] attributes){
		log.debug("Filter:" + filter);
		if (authProv != null) {
			LDAPConnection con = null;		
			try {							
				if (authProv.getSsl()) {
					con = new LDAPConnection(new LDAPJSSESecureSocketFactory());
				} else {
					con = new LDAPConnection();
				}										
				con.connect(authProv.getHost(), authProv.getPort());
				String dname  = String.format(authProv.getDn(),authProv.getUser().getName());
				con.bind(authProv.getVersion(),dname, authProv.getUser().getPassword().getBytes("UTF-8"));			
				if (con.isBound()) {								
					LDAPSearchResults res =  con.search(ldap_base,LDAPConnection.SCOPE_SUB , filter, attributes, false);
					List<Map<String,String>> entries = new ArrayList<>(res.getCount());					
					while(res.hasMore()) {
						LDAPEntry e = res.next();
						Map<String,String> a = new Hashtable<>(attributes.length);																		
						for(String key:attributes) {
							LDAPAttribute at = e.getAttribute(key);
							if (at!=null) {
								a.put(key, at.getStringValue());	
							}							
						}
						entries.add(a);
					}					
					log.debug(entries.size() + " entries found");
					return entries;
				}				
			} catch (LDAPException | UnsupportedEncodingException e) {
				log.error(e.getMessage());				
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
		return null;
	}
	
	
		
	
}
