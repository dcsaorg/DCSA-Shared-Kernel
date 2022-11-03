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
 * <p>Validates that of a set of fields at least some are not-null.</p>
 *
 * <p>Example of usage:</p>
 * <p></p><code><pre>
 * {@literal @}AtLeast(fields = {"field1", "field2" }, nonNullsRequired = 1)
 *  public record Something (
 *    private String field1,
 *    private String field2
 *  )
 * </pre></code></p>
 */
@Repeatable(AtLeast.List.class)
@Target({TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = AtLeastValidator.class)
public @interface AtLeast {
  String[] fields();

  int nonNullsRequired() default 1;

  String message() default "at least {nonNullsRequired} of {fields} must be non-null";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({TYPE_USE})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    AtLeast[] value();
  }
}
