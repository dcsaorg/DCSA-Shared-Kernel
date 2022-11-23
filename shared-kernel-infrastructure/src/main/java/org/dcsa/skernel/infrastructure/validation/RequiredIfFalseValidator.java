package org.dcsa.skernel.infrastructure.validation;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class RequiredIfFalseValidator implements ConstraintValidator<RequiredIfFalse, Object>  {
  private String ifFalse;
  private String[] thenNotNull;
  private boolean defaultTo;

  @Override
  public void initialize(RequiredIfFalse annotation) {
    ifFalse = annotation.ifFalse();
    thenNotNull = annotation.thenNotNull();
    defaultTo = annotation.defaultTo();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    BeanWrapper beanWrapper = new BeanWrapperImpl(value);

    Class<?> ifFalseType = beanWrapper.getPropertyType(ifFalse);
    if (!Boolean.class.isAssignableFrom(ifFalseType) && !boolean.class.isAssignableFrom(ifFalseType)) {
      throw new IllegalArgumentException(ifFalse + " is of type "
        + ifFalseType.getSimpleName() + " but a boolean type is expected");
    }

    Boolean ifFalseValue = Objects.requireNonNullElse((Boolean) beanWrapper.getPropertyValue(ifFalse), defaultTo);
    return ifFalseValue || allNotNull(beanWrapper);
  }

  private boolean allNotNull(BeanWrapper beanWrapper) {
    for (String s : thenNotNull) {
      if (beanWrapper.getPropertyValue(s) == null) {
        return false;
      }
    }
    return true;
  }
}
