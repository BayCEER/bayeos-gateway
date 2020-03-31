package de.unibayreuth.bayceer.bayeos.gateway;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.BasePasswordEncoder;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import de.unibayreuth.bayceer.bayeos.gateway.ldap.LdapAuthenticationProvider;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UserRepository;


@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
		
	@Value("${LDAP_AUTH:false}")
	private Boolean ldap_auth;
	
	@Value("${LDAP_DN:cn=%s}")
	private String ldap_dn;
		
	@Value("${LDAP_HOST:localhost}")
	private String ldap_host;
	
	@Value("${LDAP_PORT:636}")
	private int ldap_port;
	
	@Value("${LDAP_SSL:true}")
	private Boolean ldap_ssl;
	
	@Value("${LDAP_VERSION:3}")	
	private int ldap_version;
	
	@Value("${LDAP_REFINE_USER:true}")
	private Boolean ldap_refine_user;
	
	@Value("${LDAP_SN:sn}")
	private String ldap_sn;
		
	@Value("${LDAP_GIVEN_NAME:givenName}")
	private String ldap_givenName;
	
				
	 	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private UserRepository userRepo;
	
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(daoAuthProvider());
		if (ldap_auth) auth.authenticationProvider(ldapAuthenticationProvider());
	}
									
	@Configuration 
	@Order(1)
	public class HttpBasicConfig extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {			
			 http.csrf().disable();			 
			 http.authorizeRequests()			 	
			 	.anyRequest().authenticated()
			 	.antMatchers("/**").hasAnyRole("USER","IMPORT")
			 	.and()			 
			 .requestMatchers().antMatchers("/frame/**","/nagios/**","/grafana/**","/rest/**")
			 	.and()			 			 	
			 .httpBasic();			 
		}
		
		@Override
		protected AuthenticationManager authenticationManager() throws Exception {		
			return WebSecurityConfig.this.authenticationManager();
		}
		
		
	}
		
	
	@Configuration 
	@Order(2)
	public class FormConfig extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			 http.csrf().disable();	 
			 http.authorizeRequests()
			 .antMatchers("/resources/**").permitAll()
			 .antMatchers("/boardTemplates/**","/channelTemplates/**","/users/**","/functions/**","/invervals/**",
					 "/splines/**","/units/**","/knotpoints/**", "/domains/**", "/contacts/**", "/uploads/**").hasRole("USER")             
			 .anyRequest().authenticated()
			 .and().addFilterBefore(new TimeZoneFilter(), UsernamePasswordAuthenticationFilter.class)			 
			 //.addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)			 
			 .formLogin().loginPage("/login").successHandler(customSuccessHandler())
			 .permitAll().and().logout().permitAll();
		}
		
		@Override
		protected AuthenticationManager authenticationManager() throws Exception {		
			return WebSecurityConfig.this.authenticationManager();
		}
	}
	
	public BasePasswordEncoder basePasswordEncoder() {
		MessageDigestPasswordEncoder pwe = new ShaPasswordEncoder();
		pwe.setEncodeHashAsBase64(true);	
		return pwe;
	}
	
//	public DomainAuthenticationFilter authenticationFilter() throws Exception {
//        DomainAuthenticationFilter filter = new DomainAuthenticationFilter();
//        filter.setAuthenticationManager(authenticationManagerBean());
//        filter.setAuthenticationFailureHandler(failureHandler());
//        return filter;
//    }
	
	@Bean
    public SimpleUrlAuthenticationFailureHandler failureHandler() {
	    return new SimpleUrlAuthenticationFailureHandler("/login?error");
	}
	
	@Bean
	public AuthenticationSuccessHandler customSuccessHandler() {
		 return new CustomSuccessHandler();
	}
	
	
	
	@Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(
        		"ROLE_USER > ROLE_IMPORT\nROLE_IMPORT > ROLE_CHECK");
        return roleHierarchy;
    }

    @Bean
    public RoleVoter roleVoter() {
        return new RoleHierarchyVoter(roleHierarchy());
    }
    
    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(basePasswordEncoder());
        return authProvider;
    }
    
    @Bean 
    public LdapAuthenticationProvider ldapAuthenticationProvider() {    	
    	LdapAuthenticationProvider authProvider = new LdapAuthenticationProvider();
    	authProvider.setDn(ldap_dn);
    	authProvider.setHost(ldap_host);
    	authProvider.setPort(ldap_port);
    	authProvider.setSsl(ldap_ssl);
    	authProvider.setVersion(ldap_version);
    	authProvider.setUserRepo(userRepo); 
    	authProvider.setSn(ldap_sn);
    	authProvider.setGivenName(ldap_givenName);
    	authProvider.setRefineUser(ldap_refine_user);
    	return authProvider;
    }

	public Boolean getLdapAuth() {
		return ldap_auth;
	}
    
    
    	
    	
    
}
