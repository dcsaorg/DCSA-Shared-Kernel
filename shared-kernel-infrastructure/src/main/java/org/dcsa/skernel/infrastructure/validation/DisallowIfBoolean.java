package org.dcsa.skernel.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Repeatable(DisallowIfBoolean.List.class)
@Target({TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = DisallowIfBooleanValidator.class)
public @interface DisallowIfBoolean {
  String ifField();

  boolean hasValue();

  /**
   * Default in case the field pointed to by ifField is null.
   */
  boolean defaultTo() default false;

  String[] thenDisallow();

  String message() default "if {ifField} is {hasValue} then {thenDisallow} must be null";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({TYPE_USE})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    DisallowIfBoolean[] value();
  }
}
