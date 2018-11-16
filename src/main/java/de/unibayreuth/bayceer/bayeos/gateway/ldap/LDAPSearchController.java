package de.unibayreuth.bayceer.bayeos.gateway.ldap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

@RestController
@RequestMapping("/rest/ldap")
public class LDAPSearchController {		
	
	@Value("${LDAP_BASE:ou=users}")
	private String ldap_base;	
	
			
	private static String filter = "(&(%s=%s)(%s=%s))";	
	private Logger log = LoggerFactory.getLogger(LDAPSearchController.class);
	
	@Autowired
	LdapAuthenticationProvider authProv;
    
	@PostMapping("/search")		
	public List<String[]> search(@RequestBody LDAPQuery search) {	
		log.debug("Search:" + search);
		if (authProv != null) {
			LDAPConnection con = null;		
			try {			
				con = authProv.getCon();
				con.connect(authProv.getHost(), authProv.getPort());
				String dname  = String.format(authProv.getDn(),authProv.getUser().getName());
				con.bind(authProv.getVersion(),dname, authProv.getUser().getPassword().getBytes("UTF-8"));			
				if (con.isBound()) {
					
					LDAPSearchResults res =  con.search(ldap_base,LDAPConnection.SCOPE_SUB ,
							String.format(filter, authProv.getGivenName(),search.givenName,authProv.getSn(),search.cn), new String[] {"cn", authProv.getGivenName(),authProv.getSn()}, false);					
					LDAPEntry e;
					List<String[]> r = new ArrayList<String[]>(res.getCount());
					for(int i=0;i<res.getCount();i++) {
						e = res.next();						 
						String[] a = new String[] {
								e.getAttribute("cn").getStringValue(),
								e.getAttribute(authProv.getGivenName()).getStringValue(),
								e.getAttribute(authProv.getSn()).getStringValue()
								}; 
						r.add(a);
					}					
					return r;
					
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
