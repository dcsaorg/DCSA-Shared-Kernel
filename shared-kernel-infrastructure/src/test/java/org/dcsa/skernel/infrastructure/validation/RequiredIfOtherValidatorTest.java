package org.dcsa.skernel.infrastructure.validation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
public class RequiredIfOtherValidatorTest {
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
  public void testBothFieldsAreNull() {
    assertTrue(validate(new TestRecord(null, null)).isEmpty());
  }

  @Test
  public void testBothFieldsAreSet() {
    assertTrue(validate(new TestRecord(3.14f, "DKK")).isEmpty());
  }

  @Test
  public void testIfNotNullIsSet() {
    Set<ConstraintViolation<TestRecord>> violations = validate(new TestRecord(3.14f, null));
    assertEquals(1, violations.size());
    assertTrue(violations.stream().allMatch(v -> "if declaredValue is not null then declaredValueCurrency also must be not null".equals(v.getMessage())));
  }

  @Test
  public void testThenNotNullIsSet() {
    assertTrue(validate(new TestRecord(null, "DKK")).isEmpty());
  }

  private <T> Set<ConstraintViolation<T>> validate(T value) {
    return validatorFactory.getValidator().validate(value);
  }

  @RequiredIfOther(ifNotNull = "declaredValue", thenNotNull = "declaredValueCurrency")
  public record TestRecord(
    Float declaredValue,
    String declaredValueCurrency
  ) { }
}
