package org.dcsa.skernel.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p><b>Class-level annotation only.</b></p>
 *
 * <p>Validates that if a boolean or Boolean field is set to a false value then other fields must be not null.</p>
 *
 * <p>Example of usage:</p>
 * <p></p><code><pre>
 * {@literal @}RequiredIfFalse(ifFalse = "isElectronic", thenNotNull = "declaredValueCurrency")
 *  public record Something (
 *    private Boolean isElectronic,
 *    private String declaredValueCurrency
 *  )
 * </pre></code></p>
 */
@Repeatable(RequiredIfFalse.List.class)
@Target({TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = RequiredIfFalseValidator.class)
public @interface RequiredIfFalse {
  String ifFalse();

  /**
   * Default in case the field pointed to by ifFalse is null.
   */
  boolean defaultTo() default false;

  String[] thenNotNull();

  String message() default "if {ifFalse} is false then {thenNotNull} must be not null";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({TYPE_USE})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    RequiredIfFalse[] value();
  }
}
