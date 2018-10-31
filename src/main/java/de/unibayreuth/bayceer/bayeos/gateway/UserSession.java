package de.unibayreuth.bayceer.bayeos.gateway;

import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public interface UserSession {		
	User getUser();	
	Domain getDomain();
}
