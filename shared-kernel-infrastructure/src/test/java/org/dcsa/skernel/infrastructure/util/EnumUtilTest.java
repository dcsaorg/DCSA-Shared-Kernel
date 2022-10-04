package org.dcsa.skernel.infrastructure.util;

import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.dcsa.skernel.infrastructure.util.EnumUtil.toEnumList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnumUtilTest {
  public enum MyTestEnum {
    VALUE1, VALUE2, VALUE3
  }

  @Test
  public void testNull() {
    assertNull(toEnumList(null, MyTestEnum.class));
  }

  @Test
  public void testAllValues() {
    List<MyTestEnum> expected = List.of(MyTestEnum.VALUE1, MyTestEnum.VALUE2, MyTestEnum.VALUE3);
    assertEquals(expected, toEnumList("VALUE1, VALUE2, VALUE3", MyTestEnum.class));
  }

  // Note BadRequestException is not public so cannot access it here.
  @Test
  public void testIllegalValue() {
    assertThrows(ConcreteRequestErrorMessageException.class, () -> {
      toEnumList("VALUE4", MyTestEnum.class);
    });
  }
}
