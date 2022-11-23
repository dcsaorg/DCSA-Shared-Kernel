package org.dcsa.skernel.infrastructure.validation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
public class OneOfValidatorTest {
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
  public void testOneFieldIsSet() {
    assertTrue(validate(new TestRecord("", null, null)).isEmpty());
    assertTrue(validate(new TestRecord(null, "", null)).isEmpty());
    assertTrue(validate(new TestRecord(null, null, "")).isEmpty());
  }

  @Test
  public void testViolations() {
    testViolation(new TestRecord("", "", ""));
    testViolation(new TestRecord(null, "", ""));
    testViolation(new TestRecord("", null, ""));
    testViolation(new TestRecord("", "", null));
    testViolation(new TestRecord(null, null, null));
  }

  private void testViolation(TestRecord record) {
    Set<ConstraintViolation<TestRecord>> violations = validate(record);
    assertEquals(1, violations.size());
    assertTrue(violations.stream().allMatch(v -> "exactly one of [field1, field2, field3] must be non-null and the rest must be null".equals(v.getMessage())));
  }

  private <T> Set<ConstraintViolation<T>> validate(T value) {
    return validatorFactory.getValidator().validate(value);
  }

  @OneOf({"field1", "field2", "field3"})
  public record TestRecord(
    String field1,
    String field2,
    String field3
  ) { }
}