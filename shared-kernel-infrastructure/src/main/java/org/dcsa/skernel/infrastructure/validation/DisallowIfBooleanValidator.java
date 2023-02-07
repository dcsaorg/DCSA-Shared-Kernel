package org.dcsa.skernel.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Objects;

public class DisallowIfBooleanValidator implements ConstraintValidator<DisallowIfBoolean, Object>  {
  private String ifField;
  private boolean hasValue;
  private boolean defaultTo;
  private String[] thenDisallow;

  @Override
  public void initialize(DisallowIfBoolean annotation) {
    ifField = annotation.ifField();
    hasValue = annotation.hasValue();
    defaultTo = annotation.defaultTo();
    thenDisallow = annotation.thenDisallow();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    BeanWrapper beanWrapper = new BeanWrapperImpl(value);

    Class<?> ifFieldType = beanWrapper.getPropertyType(ifField);
    if (!Boolean.class.isAssignableFrom(ifFieldType) && !boolean.class.isAssignableFrom(ifFieldType)) {
      throw new IllegalArgumentException(ifField + " is of type "
        + ifFieldType.getSimpleName() + " but a boolean type is expected");
    }

    Boolean ifFieldValue = Objects.requireNonNullElse((Boolean) beanWrapper.getPropertyValue(ifField), defaultTo);
    return ifFieldValue != hasValue || allAreNull(beanWrapper);
  }

  private boolean allAreNull(BeanWrapper beanWrapper) {
    for (String s : thenDisallow) {
      if (beanWrapper.getPropertyValue(s) != null) {
        return false;
      }
    }
    return true;
  }
}
