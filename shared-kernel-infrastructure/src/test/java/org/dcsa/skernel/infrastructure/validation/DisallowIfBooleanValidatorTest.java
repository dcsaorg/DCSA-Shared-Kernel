package org.dcsa.skernel.infrastructure.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
public class DisallowIfBooleanValidatorTest {
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
  public void testValid() {
    assertTrue(validate(new TestRecord(false, null, null)).isEmpty());
    assertTrue(validate(new TestRecord(true, null, null)).isEmpty());
    assertTrue(validate(new TestRecord(true, "1", "2")).isEmpty());
  }

  @Test
  public void testViolations() {
    testViolation(new TestRecord(false, "1", "2"));
    testViolation(new TestRecord(false, "1", null));
    testViolation(new TestRecord(false, null, "2"));
  }

  private void testViolation(TestRecord record) {
    Set<ConstraintViolation<TestRecord>> violations = validate(record);
    assertEquals(1, violations.size());
    assertTrue(violations.stream().allMatch(v -> "if booleanField is false then [field1, field2] must be null".equals(v.getMessage())));
  }

  private <T> Set<ConstraintViolation<T>> validate(T value) {
    return validatorFactory.getValidator().validate(value);
  }

  @DisallowIfBoolean(ifField = "booleanField", hasValue = false, thenDisallow = {"field1", "field2"})
  public record TestRecord(
    Boolean booleanField,
    String field1,
    String field2
  ) { }
}
