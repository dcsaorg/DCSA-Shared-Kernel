package org.dcsa.skernel.infrastructure.validation;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class AllOrNoneValidator implements ConstraintValidator<AllOrNone, Object>  {
  private String[] fields;

  @Override
  public void initialize(AllOrNone annotation) {
    fields = annotation.value();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    BeanWrapper beanWrapper = new BeanWrapperImpl(value);
    long hasValueCount = Arrays.stream(fields)
      .filter(field -> beanWrapper.getPropertyValue(field) != null)
      .count();
    return hasValueCount == 0 || hasValueCount == fields.length;
  }
}
