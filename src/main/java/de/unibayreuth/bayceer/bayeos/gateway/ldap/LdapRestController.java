package de.unibayreuth.bayceer.bayeos.gateway.ldap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	@Value("${LDAP_HOST:localhost}")
	private String ldap_host;

	@Value("${LDAP_PORT:636}")
	private int ldap_port;

	@Value("${LDAP_SSL:true}")
	private Boolean ldap_ssl;

	@Value("${LDAP_VERSION:3}")
	private int ldap_version;

	@Value("${LDAP_SN:sn}")
	private String ldap_sn;

	@Value("${LDAP_GIVEN_NAME:givenName}")
	private String ldap_givenName;

	@Value("${LDAP_SEARCH:false}")
	public boolean ldap_search;

	private Logger log = LoggerFactory.getLogger(LdapRestController.class);

	@RequestMapping(path = "/rest/ldap/search", method = RequestMethod.GET)
	public List<Map<String, String>> search(String filter) {
		return search(filter, new String[] { "cn", ldap_givenName, ldap_sn });
	}

	@RequestMapping(path = "/rest/ldap/searchUser", method = RequestMethod.GET)
	public List<Map<String, String>> searchUser(@RequestParam String firstName, @RequestParam String lastName,
			@RequestParam String userName) {
		return search(String.format("(&(%s=%s)(%s=%s)(%s=%s))", "cn", userName, ldap_givenName, firstName, ldap_sn,
				lastName));
	}

	private List<Map<String, String>> search(String filter, String[] attributes) {
		if (ldap_search) {
			log.debug("Filter:" + filter);
			LDAPConnection con = null;
			try {
				if (ldap_ssl) {
					con = new LDAPConnection(new LDAPJSSESecureSocketFactory());
				} else {
					con = new LDAPConnection();
				}
				con.connect(ldap_host, ldap_port);
				LDAPSearchResults res = con.search(ldap_base, LDAPConnection.SCOPE_SUB, filter, attributes, false);
				List<Map<String, String>> entries = new ArrayList<>(res.getCount());
				while (res.hasMore()) {
					LDAPEntry e = res.next();
					Map<String, String> a = new Hashtable<>(attributes.length);
					for (String key : attributes) {
						LDAPAttribute at = e.getAttribute(key);
						if (at != null) {
							a.put(key, at.getStringValue());
						}
					}
					entries.add(a);
				}
				log.debug(entries.size() + " entries found");
				return entries;
			} catch (LDAPException e) {
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
