package org.dcsa.skernel.infrastructure.validation;

import org.dcsa.skernel.infrastructure.transferobject.LocationTO;

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
 * <p>Validates if a LocationTO field is only a number of types.</p>
 *
 * <p>Example: A LocationTO can be any combination of AddressLocation, FacilityLocation, UNLocation, GeoLocation.
 * And for a specific variable you want to only accept AddressLocation and FacilityLocation, but not the other subtypes:
 *
 * <code><pre>
 *   {@literal @}RestrictLocationTO({LocationType.ADDRESS, LocationType.FACILITY})
 *   private LocationTO location;
 * </pre></code>
 * </p>
 *
 * <p>null values are allowed, to disallow null use {@literal @}NotNull in combination with this.</p>
 * <p>This validation can only be applied on LocationTO types.</p>
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = RestrictLocationTOValidator.class)
public @interface RestrictLocationTO {
  /**
   * The types allowed.
   */
  LocationTO.LocationType[] value();

  String message() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
