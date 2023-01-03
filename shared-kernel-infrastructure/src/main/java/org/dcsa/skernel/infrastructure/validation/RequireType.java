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
 * <p>Validates if a field is one of a number of types.</p>
 *
 * <p>Example: You have AddressLocation, FacilityLocation, UNLocation, GeoLocation that are all subtypes of Location.
 * And for a specific variable you want to only accept AddressLocation and FacilityLocation, but not the other subtypes:
 *
 * <code><pre>
 *   {@literal @}RequireType(
 *     value = {AddressLocation.class, FacilityLocation.class},
 *     message = "location must be an AddressLocation or a FacilityLocation"
 *   )
 *   private Location location;
 * </pre></code>
 * </p>
 *
 * <p>null values are allowed, to disallow null use {@literal @}NotNull in combination with this.</p>
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = RequireTypeValidator.class)
public @interface RequireType {
  /**
   * The types allowed.
   */
  Class<?>[] value();

  /**
   * Message if validation fails, it is recommended to specify this as the default shows the fully qualified names of
   * all allowed types.
   */
  String message() default "must be one of the following types: {value}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
