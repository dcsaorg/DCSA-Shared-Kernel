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
    T[] enumValues = enumClass.getEnumConstants();
    return Arrays.stream(values.split(","))
      .map(String::trim)
      .map(s -> Arrays.stream(enumValues)
        .filter(v -> v.name().equals(s))
        .findFirst()
        .orElseThrow(() -> ConcreteRequestErrorMessageException.invalidInput(
          "The value '" + s + "' cannot be mapped to the enum " + enumClass.getSimpleName()))
      )
      .toList();
  }
}
