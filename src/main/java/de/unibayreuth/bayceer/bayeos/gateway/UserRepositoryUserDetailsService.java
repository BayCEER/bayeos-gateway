package de.unibayreuth.bayceer.bayeos.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UserRepository;


@Service
public class UserRepositoryUserDetailsService implements UserDetailsService {
	
	@Autowired
    private UserRepository userRepository;

	private Logger log = LoggerFactory.getLogger(UserRepositoryUserDetailsService.class);

    
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {    
    	log.debug("Load user:" + name);    	
    	String[] context = name.split("@");
        User user;
    	if (context.length < 2) {
    		user = userRepository.findFirstByNameIgnoreCaseAndDomainIsNullAndLockedIsFalseAndPasswordIsNotNull(context[0]);
    	} else {
    		user = userRepository.findFirstByNameIgnoreCaseAndDomainNameIgnoreCaseAndLockedIsFalseAndPasswordIsNotNull(context[0],context[1]);
    	}    		               
        if(user == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        } 
        return new CustomUserDetails(user);
    }
       
    
    
        
    
         
}
