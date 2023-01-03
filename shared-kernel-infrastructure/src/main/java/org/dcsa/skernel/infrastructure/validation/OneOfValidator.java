package org.dcsa.skernel.infrastructure.validation;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class OneOfValidator implements ConstraintValidator<OneOf, Object>  {
  private String[] fields;

  @Override
  public void initialize(OneOf annotation) {
    fields = annotation.value();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    BeanWrapper beanWrapper = new BeanWrapperImpl(value);
    long hasValueCount = Arrays.stream(fields)
      .filter(field -> beanWrapper.getPropertyValue(field) != null)
      .count();
    return hasValueCount == 1;
  }
}
