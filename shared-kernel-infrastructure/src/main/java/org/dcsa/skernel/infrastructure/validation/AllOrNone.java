package org.dcsa.skernel.infrastructure.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p><b>Class-level annotation only.</b></p>
 *
 * <p>Validates that if one field is set to a non-null value then another field is also not null and vice-versa.</p>
 *
 * <p>Example of usage:</p>
 * <p></p><code><pre>
 * {@literal @}AllOrNone({"declaredValue", "declaredValueCurrency" })
 *  public record Something (
 *    private Float declaredValue,
 *    private String declaredValueCurrency
 *  )
 * </pre></code></p>
 */
@Repeatable(AllOrNone.List.class)
@Target({TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = AllOrNoneValidator.class)
public @interface AllOrNone {
  String[] value();

  String message() default "all of {value} must be either null or non-null";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({TYPE_USE})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    AllOrNone[] value();
  }
}
