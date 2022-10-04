package org.dcsa.skernel.infrastructure.util;

import lombok.experimental.UtilityClass;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class EnumUtil {
  /**
   * Splits a comma-separated string and converts each subpart into
   * an enum.
   */
  public static <T extends Enum<T>> List<T> toEnumList(String values, Class<T> enumClass) {
    if (values == null) {
      return null;
    }
    try {
      return Arrays.stream(values.split(","))
        .map(s -> Enum.valueOf(enumClass, s.trim()))
        .toList();
    } catch (IllegalArgumentException e) {
      throw ConcreteRequestErrorMessageException.invalidInput(e.getMessage());
    }
  }
}
