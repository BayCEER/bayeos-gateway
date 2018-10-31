package de.unibayreuth.bayceer.bayeos.gateway.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy=FunctionExistsValidator.class)
public @interface FunctionExists {
	 String message() default "{validation.functionNotExistent}";
	 Class[] groups() default {};
	 Class[] payload() default {};

}
