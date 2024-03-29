package org.dcsa.skernel.infrastructure.validation;

import org.dcsa.skernel.infrastructure.util.ReflectUtility;

import jakarta.el.MethodNotFoundException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RequiredIfTrueValidator implements ConstraintValidator<RequiredIfTrue, Object> {

  private String isFieldReferenceRequired;
  private String fieldReference;

  @Override
  public void initialize(RequiredIfTrue constraintAnnotation) {
    this.isFieldReferenceRequired = constraintAnnotation.isFieldReferenceRequired();
    this.fieldReference = constraintAnnotation.fieldReference();
  }

  @Override
  public boolean isValid(Object entity, ConstraintValidatorContext constraintValidatorContext) {
    if (null == entity) {
      return false;
    }

    return isValidImpl(entity, constraintValidatorContext);
  }

  private boolean isValidImpl(Object entity, ConstraintValidatorContext constraintValidatorContext) {
    Method isFieldReferenceRequiredGetter, fieldReferenceGetter;
    boolean isFieldReferenceRequiredValue;
    boolean isFieldReferenceValueNull;
    try {
      isFieldReferenceRequiredGetter = ReflectUtility.getGetterMethodFromName(entity.getClass(), isFieldReferenceRequired);
      fieldReferenceGetter = ReflectUtility.getGetterMethodFromName(entity.getClass(), fieldReference);
    } catch (MethodNotFoundException e) {
      throw new IllegalStateException(
        "Invalid @RequiredIfTrueValidator annotation for entity "
          + entity.getClass().getSimpleName()
          + ": The entity does not have both of the fields "
          + isFieldReferenceRequired
          + " and "
          + fieldReference,
        e);
    }

    if (!(Boolean.class.isAssignableFrom(isFieldReferenceRequiredGetter.getReturnType()) || boolean.class.isAssignableFrom(isFieldReferenceRequiredGetter.getReturnType()))) {
      throw new IllegalStateException("Return type is not boolean!");
    }

    try {
      isFieldReferenceRequiredValue = cast(isFieldReferenceRequiredGetter.invoke(entity));
      isFieldReferenceValueNull = fieldReferenceGetter.invoke(entity) == null;
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
        "Issue with @RequiredIfTrueValidator annotation for entity "
          + entity.getClass().getSimpleName()
          + ": The getter for "
          + isFieldReferenceRequired
          + " or "
          + fieldReference
          + " triggered an exception!");
    }
    return !isFieldReferenceRequiredValue || !isFieldReferenceValueNull;
  }

  //   Helper to deal compiler warnings about unchecked casts.
  private static boolean cast(Object value) {
    return value != null && (Boolean) value;
  }
}
