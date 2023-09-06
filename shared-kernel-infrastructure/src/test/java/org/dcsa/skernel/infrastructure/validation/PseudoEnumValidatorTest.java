package org.dcsa.skernel.infrastructure.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class PseudoEnumValidatorTest {
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
  void testFieldsAreValid() {
    assertTrue(validate(new TestRecord("AA", "BB")).isEmpty());
  }

  @Test
  void testEnumAInvalid() {
    var violations = validate(new TestRecord("BA", "BB"));
    assertEquals(1, violations.size());
    var violation = violations.iterator().next();
    assertEquals("enumA", violation.getPropertyPath().toString());
    assertEquals("The value should have been one of: AA, AB, AC, AD, AE, AF", violation.getMessage());
  }

  @Test
  void testEnumBInvalid() {
    var violations = validate(new TestRecord("AA", "AA"));
    assertEquals(1, violations.size());
    var violation = violations.iterator().next();
    assertEquals("enumB", violation.getPropertyPath().toString());
    assertEquals("The value should have been one of: BA, BB, BC, BD, BE, BF", violation.getMessage());
  }


  private <T> Set<ConstraintViolation<T>> validate(T value) {
    return validatorFactory.getValidator().validate(value);
  }


  public record TestRecord(
     @PseudoEnum("pseudoenum.csv")
     String enumA,
    @PseudoEnum(value = "pseudoenum.csv", column = "Column B")
    String enumB
  ) { }
}
