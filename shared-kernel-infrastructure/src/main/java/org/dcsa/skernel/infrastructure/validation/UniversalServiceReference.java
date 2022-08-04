package org.dcsa.skernel.infrastructure.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates a given string is a valid Universal Service Reference
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = UniversalServiceReferenceValidator.class)
public @interface UniversalServiceReference {

  String message() default "must contain a valid universal service reference (SRXXXXXY)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
