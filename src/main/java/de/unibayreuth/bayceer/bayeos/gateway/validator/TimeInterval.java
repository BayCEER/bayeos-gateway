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
@Constraint(validatedBy=TimeIntervalValidator.class)
public @interface TimeInterval {
	 String message() default "{validation.timeIntervalInvalid}";
	 Class[] groups() default {};
	 Class[] payload() default {};

}
