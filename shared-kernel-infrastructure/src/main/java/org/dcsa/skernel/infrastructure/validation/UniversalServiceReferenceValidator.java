package org.dcsa.skernel.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class UniversalServiceReferenceValidator implements ConstraintValidator<UniversalServiceReference, String> {

  private static final String SRXXXXXY = "SRXXXXXY";
  private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^\\d+$");

  private static final int[][] DAMM_TABLE = {
    {0, 3, 1, 7, 5, 9, 8, 6, 4, 2},
    {7, 0, 9, 2, 1, 5, 4, 8, 6, 3},
    {4, 2, 0, 6, 8, 7, 1, 3, 5, 9},
    {1, 7, 5, 0, 9, 8, 3, 4, 2, 6},
    {6, 1, 2, 3, 0, 4, 5, 9, 7, 8},
    {3, 6, 7, 4, 2, 0, 9, 5, 8, 1},
    {5, 8, 6, 9, 7, 2, 0, 1, 3, 4},
    {8, 9, 4, 5, 3, 6, 2, 0, 1, 7},
    {9, 4, 3, 8, 6, 1, 7, 2, 0, 5},
    {2, 5, 8, 1, 4, 3, 6, 7, 9, 0}
  };

  @Override
  public void initialize(UniversalServiceReference constraintAnnotation) {
  }

  @Override
  public boolean isValid(String entity, ConstraintValidatorContext constraintValidatorContext) {
    if (null == entity) {
      return true;
    }
    return this.isValidImpl(entity, constraintValidatorContext);
  }

  private int runDamm(String nr) {
    int i = 0;
    Iterable<Integer> nrIter = nr.chars()::iterator;
    for (int digit : nrIter) {
      i = DAMM_TABLE[i][digit - '0'];
    }
    return i;
  }

  private boolean isValidImpl(String entity, ConstraintValidatorContext constraintValidatorContext) {
    if (entity.length() != SRXXXXXY.length()) {
      return false;
    }
    if (!entity.equals(entity.toUpperCase())) {
      return false;
    }
    if (!entity.startsWith("SR")) {
      return false;
    }
    char checkChar = entity.charAt(entity.length() - 1);
    String nr = entity.substring(2, entity.length() - 1);
    if (checkChar < 'A' || checkChar >= 'Z') {
      return false;
    }
    int actualCheckDigit = checkChar - 'A';
    if (!DIGITS_ONLY_PATTERN.matcher(nr).matches()) {
      return false;
    }
    int expectedCheckDigit = runDamm(nr);
    return actualCheckDigit == expectedCheckDigit;
  }

}
