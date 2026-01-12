package de.unibayreuth.bayceer.bayeos.gateway.model;

public class Permission {

public static boolean readable(User u,DomainEntity d) {       
    return domainAccess(u,d) && (u.getRole().equals(Role.USER) 
            || u.getRole().equals(Role.CHECK)); 
}

public static boolean writeable(User u,DomainEntity d ) {        
    return domainAccess(u, d) && u.getRole().equals(Role.USER);
}


private static boolean domainAccess(User u, DomainEntity d) {    
   if (u.domain == null) {
       return true;
   } else {
       return u.domain == d.domain;
   }
    
}
}
