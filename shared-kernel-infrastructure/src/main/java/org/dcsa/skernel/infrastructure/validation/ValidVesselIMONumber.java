package org.dcsa.skernel.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validate that a given String field contains a valid vessel IMO number
 *
 * This constraint validation ensures that the value of a String field
 * includes a vessel IMO number by ensuring that:
 *
 * <ol>
 *     <li>The value is a String consisting of a 7 digit number.</li>
 *     <li>The IMO "check" digit of the number is valid.
 *     (See <a>https://en.wikipedia.org/wiki/IMO_number#Structure</a> for details)</li>
 * </ol>
 *
 * Note that the official IMO standard includes an "IMO" prefix to all numbers.  In the
 * DCSA API context, the IMO prefix is always excluded (as it is redundant).  The
 * validator will therefore reject IMO numbers with the prefix for this reason.
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = VesselIMONumberValidator.class)
public @interface ValidVesselIMONumber {

    boolean allowNull() default false;

    String message() default "must be a valid Vessel IMO Number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
