package org.dcsa.skernel.infrastructure.validation;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AllOrNoneValidatorTest {
  @Test
  public void testAllFieldsAreNull() {
    assertTrue(validate(new TestRecord(null, null, null)).isEmpty());
  }

  @Test
  public void testAllFieldsAreSet() {
    assertTrue(validate(new TestRecord(100f, 25f, "DKK")).isEmpty());
  }

  @Test
  public void testViolation() {
    Set<ConstraintViolation<TestRecord>> violations = validate(new TestRecord(100f, null, "DKK"));
    assertEquals(1, violations.size());
    System.out.println(violations);
    assertTrue(violations.stream().allMatch(v -> "all of [price, vatAmount, currency] must be either null or non-null".equals(v.getMessage())));
  }

  private <T> Set<ConstraintViolation<T>> validate(T value) {
    try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      Validator validator = validatorFactory.getValidator();
      return validator.validate(value);
    }
  }

  @AllOrNone({"price", "vatAmount", "currency" })
  public record TestRecord(
    Float price,
    Float vatAmount,
    String currency
  ) { }
}
