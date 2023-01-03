package org.dcsa.skernel.infrastructure.validation;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class AtLeastValidator implements ConstraintValidator<AtLeast, Object>  {
  private String[] fields;
  private int nonNullsRequired;

  @Override
  public void initialize(AtLeast annotation) {
    fields = annotation.fields();
    nonNullsRequired = annotation.nonNullsRequired();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    BeanWrapper beanWrapper = new BeanWrapperImpl(value);
    long hasValueCount = Arrays.stream(fields)
      .filter(field -> beanWrapper.getPropertyValue(field) != null)
      .count();
    return hasValueCount >= nonNullsRequired;
  }
}
