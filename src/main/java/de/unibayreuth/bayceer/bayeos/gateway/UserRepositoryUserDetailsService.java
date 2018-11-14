package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UserRepository;


@Service
public class UserRepositoryUserDetailsService implements UserDetailsService {
	
	@Autowired
    private UserRepository userRepository;
        
    
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {    	
    	String[] context = StringUtils.split(name, "@");
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
