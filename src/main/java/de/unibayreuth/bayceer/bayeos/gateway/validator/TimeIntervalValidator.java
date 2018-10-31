package de.unibayreuth.bayceer.bayeos.gateway.validator;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

public class TimeIntervalValidator implements ConstraintValidator<TimeInterval, String> {
	
	@Autowired
	EntityManager em;

	@Override
	public void initialize(TimeInterval constraintAnnotation) {
		
	}

	@Override
	public boolean isValid(String name, ConstraintValidatorContext context) {		
		try {	
 			Query q = em.createNativeQuery("select EXTRACT(MILLISECONDS FROM INTERVAL '" + name + "')");
			q.getResultList();		
			return true;
		} catch (Exception e) {			
			return false;			
		}		
		
	}

}
