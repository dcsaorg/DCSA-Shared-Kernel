package org.dcsa.skernel.infrastructure.validation;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequiredIfOtherValidator implements ConstraintValidator<RequiredIfOther, Object>  {
  private String ifNotNull;
  private String thenNotNull;

  @Override
  public void initialize(RequiredIfOther annotation) {
    ifNotNull = annotation.ifNotNull();
    thenNotNull = annotation.thenNotNull();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    BeanWrapper beanWrapper = new BeanWrapperImpl(value);
    Object ifNotNullValue = beanWrapper.getPropertyValue(ifNotNull);
    return ifNotNullValue == null || beanWrapper.getPropertyValue(thenNotNull) != null;
  }
}
