package org.dcsa.skernel.infrastructure.validation;

import org.apache.commons.lang3.EnumUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.stream.Stream;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

  private Class<? extends Enum> clazz;

  @Override
  public void initialize(ValidEnum constraintAnnotation) {
    this.clazz = constraintAnnotation.clazz();
  }

  @Override
  public boolean isValid(String values, ConstraintValidatorContext constraintValidatorContext) {

    if (null == values) {
      return true;
    }

    Stream<String> enumStream =
      values.contains(",") ? Arrays.stream(values.split(",")) : Stream.of(values);

    return enumStream.allMatch(e -> EnumUtils.isValidEnum(clazz, e));
  }
}
