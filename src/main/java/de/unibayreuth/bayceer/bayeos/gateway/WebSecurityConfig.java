package de.unibayreuth.bayceer.bayeos.gateway;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
	     
		 http.csrf().disable();
		 http.authorizeRequests()
             .antMatchers("/resources/**").permitAll()
             .antMatchers("/Frame/saveFrame").hasAnyRole("ADMIN","USER","IMPORT")
             .antMatchers("/admin/**").hasRole("ADMIN")             
             .anyRequest().authenticated()             
             .and()
             .addFilterBefore(new TimeZoneFilter(), UsernamePasswordAuthenticationFilter.class)
          .formLogin()
             .loginPage("/login")
             .permitAll()             
             .and()
         .httpBasic()
          	.and()
         .logout()                                    
            .permitAll();
		 
	}
	
	
	
	@Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, UserDetailsService userDetailsService) throws Exception {
		MessageDigestPasswordEncoder pwe = new ShaPasswordEncoder();
		pwe.setEncodeHashAsBase64(true);		
        auth.userDetailsService(userDetailsService).passwordEncoder(pwe);              
    }
	
	@Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(
        		"ROLE_ADMIN > ROLE_USER\nROLE_USER > ROLE_IMPORT\nROLE_IMPORT > ROLE_CHECK");
        return roleHierarchy;
    }

    @Bean
    public RoleVoter roleVoter() {
        return new RoleHierarchyVoter(roleHierarchy());
    }


	
   
	

}