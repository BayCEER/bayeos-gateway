package de.unibayreuth.bayceer.bayeos.gateway;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public class CustomUserDetails extends User implements UserDetails {

        public CustomUserDetails(User user) {
            super(user);            
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return AuthorityUtils.createAuthorityList("ROLE_" + super.getRole().toString());
        }


        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return !getLocked();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        private static final long serialVersionUID = 5639683223516504866L;

		@Override
		public String getUsername() {
			return name;			
		}
		
				
		
    }