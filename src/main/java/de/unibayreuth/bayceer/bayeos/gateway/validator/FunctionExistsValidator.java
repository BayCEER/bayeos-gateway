package de.unibayreuth.bayceer.bayeos.gateway.validator;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

public class FunctionExistsValidator implements ConstraintValidator<FunctionExists, String> {
	
	@Autowired
	EntityManager em;

	@Override
	public void initialize(FunctionExists constraintAnnotation) {
		
	}

	@Override
	public boolean isValid(String name, ConstraintValidatorContext context) {		
		try {	
 			Query q = em.createNativeQuery("select " + name + "(1);");
			q.getResultList();						
			return true;
		} catch (Exception e) {			
			return false;			
		}		
		
	}

}
