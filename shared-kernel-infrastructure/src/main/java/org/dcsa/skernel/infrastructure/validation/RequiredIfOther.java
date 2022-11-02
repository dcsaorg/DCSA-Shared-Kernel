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
 * <p>Validates that if one field is set to a non-null value then another field is also not null.</p>
 *
 * <p>Example of usage:</p>
 * <p></p><code><pre>
 * {@literal @}RequiredIfOther(ifNotNull = "declaredValue", thenNotNull = "declaredValueCurrency")
 *  public record Something (
 *    private Float declaredValue,
 *    private String declaredValueCurrency
 *  )
 * </pre></code></p>
 *
 * <p>Note this does not enforce that both field are either null or non-null, for that behaviour use {@literal @}AllOrNone</p>
 */
@Repeatable(RequiredIfOther.List.class)
@Target({TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = RequiredIfOtherValidator.class)
public @interface RequiredIfOther {
  String ifNotNull();

  String thenNotNull();

  String message() default "if {ifNotNull} is not null then {thenNotNull} also must be not null";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({TYPE_USE})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    RequiredIfOther[] value();
  }
}
