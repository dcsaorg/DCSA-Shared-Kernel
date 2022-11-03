package org.dcsa.skernel.infrastructure.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class RequireTypeValidator implements ConstraintValidator<RequireType, Object>  {
  private Class<?>[] validTypes;

  @Override
  public void initialize(RequireType annotation) {
    validTypes = annotation.value();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    return (value == null) || Arrays.stream(validTypes).anyMatch(validType -> validType.isInstance(value));
  }
}
