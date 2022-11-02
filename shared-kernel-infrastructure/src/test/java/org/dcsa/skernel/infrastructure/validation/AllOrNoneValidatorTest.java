package org.dcsa.skernel.infrastructure.validation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
public class AllOrNoneValidatorTest {
  private ValidatorFactory validatorFactory;

  @BeforeAll
  public void createValidatorFactory() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
  }

  @AfterAll
  public void closeValidatorFactory() {
    validatorFactory.close();
  }

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
    assertTrue(violations.stream().allMatch(v -> "all of [price, vatAmount, currency] must be null or all must be non-null".equals(v.getMessage())));
  }

  private <T> Set<ConstraintViolation<T>> validate(T value) {
    return validatorFactory.getValidator().validate(value);
  }

  @AllOrNone({"price", "vatAmount", "currency" })
  public record TestRecord(
    Float price,
    Float vatAmount,
    String currency
  ) { }
}
