package org.dcsa.skernel.test.helpers;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper to check if enums contains the same values.
 */
@UtilityClass
public class EnumValidator {
  public static <S extends Enum<S>, T extends Enum<T>> void assertHaveSameValues(Class<S> src, Class<T> target) {
    Assertions.assertEquals(Collections.emptyList(), getMissingValues(src, target));
  }

  public static <S extends Enum<S>, T extends Enum<T>> List<String> getMissingValues(Class<S> src, Class<T> target) {
    List<String> result = getValuesMissingInSrc(src, target);
    result.addAll(getValuesMissingInSrc(target, src));
    return result;
  }

  private static <S extends Enum<S>, T extends Enum<T>> List<String> getValuesMissingInSrc(Class<S> src, Class<T> target) {
    Set<String> srcValues = Arrays.stream(src.getEnumConstants()).map(Enum::name).collect(Collectors.toSet());
    Set<String> targetValues = Arrays.stream(target.getEnumConstants()).map(Enum::name).collect(Collectors.toSet());
    targetValues.removeAll(srcValues);
    return targetValues.stream().map(name -> src.getSimpleName() + "." + name).collect(Collectors.toList());
  }
}
