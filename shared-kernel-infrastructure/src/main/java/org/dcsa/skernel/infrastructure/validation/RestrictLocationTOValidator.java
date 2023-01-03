package org.dcsa.skernel.infrastructure.validation;

import org.dcsa.skernel.infrastructure.transferobject.LocationTO;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO.LocationType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.EnumSet;

public class RestrictLocationTOValidator implements ConstraintValidator<RestrictLocationTO, LocationTO> {
  private EnumSet<LocationType> validTypes;
  private boolean customMessage;

  @Override
  public void initialize(RestrictLocationTO annotation) {
    validTypes = EnumSet.copyOf(Arrays.stream(annotation.value()).toList());
    customMessage = annotation.message() != null && !"".equals(annotation.message());
  }

  @Override
  public boolean isValid(LocationTO value, ConstraintValidatorContext context) {
    boolean result = value == null || isValid(value);
    if (!result && !customMessage) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
        "must be one or more of the following location types: "
          + validTypes.stream().map(LocationType::getPrettyName).toList()).addConstraintViolation();
    }
    return result;
  }

  private boolean isValid(LocationTO value) {
    EnumSet<LocationType> invalidTypes = EnumSet.complementOf(validTypes);
    return validTypes.stream().map(LocationType::getIsType).anyMatch(p -> p.test(value))
      && invalidTypes.stream().map(LocationType::getMightNotBeType).allMatch(p -> p.test(value));
  }
}
