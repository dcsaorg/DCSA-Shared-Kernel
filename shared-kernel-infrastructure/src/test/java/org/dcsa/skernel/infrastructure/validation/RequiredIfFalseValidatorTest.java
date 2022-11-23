package org.dcsa.skernel.infrastructure.validation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(Lifecycle.PER_CLASS)
public class RequiredIfFalseValidatorTest {
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
  public void testWithNullBoolean() {
    assertValid(new BooleanTestRecord(null, "s1", "s2", "s3"));
    assertInvalid(new BooleanTestRecord(null, null, "s2", "s3"));
    assertValid(new BooleanWithTrueDefaultTestRecord(null, null));
    assertValid(new BooleanWithTrueDefaultTestRecord(true, null));
  }

  @Test
  public void testWithNonNullBoolean() {
    assertValid(new BooleanTestRecord(false, "s1", "s2", "s3"));
    assertInvalid(new BooleanTestRecord(false, null, "s2", "s3"));
    assertValid(new BooleanTestRecord(true, null, null, null));
  }

  @Test
  public void testNotBooleanField() {
    assertThrows(ValidationException.class, () -> isValid(new NotABooleanTestRecord(null, "s")));
    assertThrows(ValidationException.class, () -> isValid(new NotABooleanTestRecord("s", "s")));
  }

  @Test
  public void testPrimitives() {
    assertValid(new PrimitiveBooleanTestRecord(true, null));
    assertValid(new PrimitiveBooleanTestRecord(false, "s"));
    assertInvalid(new PrimitiveBooleanTestRecord(false, null));
  }

  private <T> void assertValid(T value) {
    assertTrue(isValid(value));
  }

  private <T> void assertInvalid(T value) {
    assertFalse(isValid(value));
  }

  private <T> boolean isValid(T value) {
    return validatorFactory.getValidator().validate(value).isEmpty();
  }

  @RequiredIfFalse(ifFalse = "ifFalse", thenNotNull = { "thenNotNUll1",  "thenNotNUll2",  "thenNotNUll3" })
  public record BooleanTestRecord(
    Boolean ifFalse,
    String thenNotNUll1,
    String thenNotNUll2,
    String thenNotNUll3
  ) { }

  @RequiredIfFalse(ifFalse = "ifFalse", defaultTo = true, thenNotNull = "thenNotNUll")
  public record BooleanWithTrueDefaultTestRecord(
    Boolean ifFalse,
    String thenNotNUll
  ) { }

  @RequiredIfFalse(ifFalse = "ifFalse", thenNotNull = "thenNotNUll")
  public record PrimitiveBooleanTestRecord(
    boolean ifFalse,
    String thenNotNUll
  ) { }

  @RequiredIfFalse(ifFalse = "ifFalse", thenNotNull = "thenNotNUll")
  public record NotABooleanTestRecord(
    String ifFalse,
    String thenNotNUll
  ) { }
}
