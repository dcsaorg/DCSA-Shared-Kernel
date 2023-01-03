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
public class RequireTypeValidatorTest {
  interface Parent { }
  record ChildA(String s) implements Parent { }
  record ChildB(String s) implements Parent { }
  record ChildC(String s) implements Parent { }
  record ChildD(String s) implements Parent { }

  record Require(
    @RequireType(value = {ChildA.class, ChildC.class}, message = "field must be a ChildA or ChildC")
    RequireTypeValidatorTest.Parent field
  ) {}

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
    assertTrue(validate(new Require(null)).isEmpty());
    assertTrue(validate(new Require(new ChildA(""))).isEmpty());
    assertTrue(validate(new Require(new ChildC(""))).isEmpty());
  }

  @Test
  public void testViolations() {
    testViolation(new Require(new ChildB("")));
    testViolation(new Require(new ChildD("")));
  }

  private void testViolation(Require record) {
    Set<ConstraintViolation<Require>> violations = validate(record);
    assertEquals(1, violations.size());
    assertTrue(violations.stream().allMatch(v -> ("field must be a ChildA or ChildC").equals(v.getMessage())));
  }

  private <T> Set<ConstraintViolation<T>> validate(T value) {
    return validatorFactory.getValidator().validate(value);
  }
}
