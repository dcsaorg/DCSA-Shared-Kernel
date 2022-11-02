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
 * <p>Validates that of a set of fields exactly one of them must be not-null.</p>
 *
 * <p>Example of usage:</p>
 * <p></p><code><pre>
 * {@literal @}OneOf({"field1", "field2" })
 *  public record Something (
 *    private String field1,
 *    private String field2
 *  )
 * </pre></code></p>
 */
@Repeatable(OneOf.List.class)
@Target({TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = OneOfValidator.class)
public @interface OneOf {
  String[] value();

  String message() default "exactly one of {value} must be non-null and the rest must be null";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({TYPE_USE})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    OneOf[] value();
  }
}
