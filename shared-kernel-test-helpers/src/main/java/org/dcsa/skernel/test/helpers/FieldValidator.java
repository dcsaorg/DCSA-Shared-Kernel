package org.dcsa.skernel.test.helpers;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper to see if some classes are missing fields that are present in other classes.
 */
@UtilityClass
public class FieldValidator {

  /**
   * Returns the names of all fields that are present in one class but not in the other.
   */
  public static <S, T> List<String> getMissingFields(Class<S> src, Class<T> target, String... excludeFields) {
    List<String> result = getFieldsMissingInSrc(src, target, excludeFields);
    result.addAll(getFieldsMissingInSrc(target, src, excludeFields));
    return result;
  }

  /**
   * Returns the names of all fields that are present in the target class but not in the src class.
   */
  public static <S, T> List<String> getFieldsMissingInSrc(Class<S> src, Class<T> target, String... excludeFields) {
    Set<String> srcFields = getFields(src);
    Set<String> targetFields = getFields(target);
    targetFields.removeAll(srcFields);
    targetFields.removeAll(Set.of(excludeFields));
    return targetFields.stream().map(name -> src.getSimpleName() + "." + name).collect(Collectors.toList());
  }

  /**
   * Assert that all fields present in the target are also present in the src except any excluded fields.
   */
  public static <S, T> void assertTargetFieldsPresentInSrc(Class<S> src, Class<T> target, String... excludeFields) {
    Assertions.assertEquals(Collections.emptyList(), getFieldsMissingInSrc(src, target, excludeFields));
  }

  /**
   * Assert that all fields present in the target are also present in the src and vice versa except any excluded fields.
   */
  public static <S, T> void assertFieldsAreEqual(Class<S> src, Class<T> target, String... excludeFields) {
    Assertions.assertEquals(Collections.emptyList(), getMissingFields(src, target, excludeFields));
  }

  private static Set<String> getFields(Class<?> cl) {
    Set<String> fields = new HashSet<>();
    Class<?> current = cl;
    while (current != null) {
      Arrays.stream(current.getDeclaredFields())
        .filter(f -> (f.getModifiers() & (Modifier.STATIC|Modifier.TRANSIENT)) == 0)
        .map(Field::getName)
        .forEach(fields::add);
      current = current.getSuperclass();
    }
    return fields;
  }
}
