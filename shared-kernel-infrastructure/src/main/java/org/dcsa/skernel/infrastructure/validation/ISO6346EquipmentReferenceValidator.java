package org.dcsa.skernel.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

// Implements the logic of the BIC container validation (part of ISO 6346)
// https://en.wikipedia.org/wiki/ISO_6346
public class ISO6346EquipmentReferenceValidator implements ConstraintValidator<ISO6346EquipmentReference, String> {

  private static final int EQUIPMENT_REFERENCE_LENGTH = 11;
  private static final int EQUIPMENT_REFERENCE_SUM_CHARS = 10;

  private static final Pattern EQUIPMENT_REFERENCE_PATTERN = Pattern.compile("[A-Z]{3}[UJZ]\\d{7}");

  private static final int[] LETTER_VALUE_LOOKUP = {
    10, 12, 13, 14, 15, 16, 17, 18, 19, 20, /* A-J */
    21, 23, 24, 25, 26, 27, 28, 29, 30, 31, /* K-T */
    32, 34, 35, 36, 37, 38, 39,             /* U-Z */
  };

  @Override
  public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
    if (s == null) {
      return true;
    }
    return checkEquipmentReference(s);
  }

  private static int charToValue(char c) {
    if ('0' <= c && c <= '9') {
      return c - '0';
    }
    if ('A' <= c && c <= 'Z') {
      return LETTER_VALUE_LOOKUP[c - 'A'];
    }
    throw new AssertionError("Invalid character " + c);
  }

  private static int computeReferenceSum(String r) {
    int sum = 0;
    for (int i = 0 ; i < EQUIPMENT_REFERENCE_SUM_CHARS ; i++) {
      // Bit-shifting works as a 2^n multiplication
      sum += charToValue(r.charAt(i)) << i;
    }
    return sum;
  }

  private static boolean checkEquipmentReference(String r) {
    if (r.length() != EQUIPMENT_REFERENCE_LENGTH) {
      return false;
    }
    if (!EQUIPMENT_REFERENCE_PATTERN.matcher(r).matches()) {
      return false;
    }
    // The first 3 letters are the owner number and strictly speaking, there
    // is a table of valid owners.  However, we are not validating those as
    // that would require additional maintenance.
    int sum = computeReferenceSum(r);
    // The "% 10" is because that the check digit is always a digit meaning
    // that 10 (which should have been an 'A') is not a valid number for the
    // check digit.
    // BIC resolves this by replacing 10 with 0 (which happens to be a "% 10").
    int checkDigit = (sum % 11) % 10;
    return checkDigit == charToValue(r.charAt(EQUIPMENT_REFERENCE_LENGTH - 1));
  }

  @Override
  public void initialize(ISO6346EquipmentReference constraintAnnotation) {
    // Do nothing
  }
}
