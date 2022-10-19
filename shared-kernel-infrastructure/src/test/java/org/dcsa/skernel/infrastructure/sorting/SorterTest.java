package org.dcsa.skernel.infrastructure.sorting;

import lombok.Value;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.infrastructure.pagination.Cursor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SorterTest {

  private Sorter sorter;

  @BeforeEach
  void init() {
    sorter =
        new Sorter(List.of(new Cursor.SortBy(Sort.Direction.ASC, "field1")), "field1", "field2");
  }

  @Test
  void testSorterParseSort_WithDefaultSort() {
    List<Cursor.SortBy> sortList = sorter.parseSort(null);
    assertEquals(1, sortList.size());
    assertEquals(Sort.Direction.ASC, sortList.get(0).direction());
    assertEquals("field1", sortList.get(0).field());
  }

  @Test
  void testSorterParseSort_tWithDescSortOnField() {
    List<Cursor.SortBy> sortList = sorter.parseSort("field2:DESC");
    assertEquals(1, sortList.size());
    assertEquals(Sort.Direction.DESC, sortList.get(0).direction());
    assertEquals("field2", sortList.get(0).field());
  }

  @Test
  void testSorterParseSort_WithMultipleSortFields() {
    List<Cursor.SortBy> sortList = sorter.parseSort("field2:DESC,field1:ASC");
    assertEquals(2, sortList.size());
    assertEquals(Sort.Direction.DESC, sortList.get(0).direction());
    assertEquals("field2", sortList.get(0).field());
    assertEquals(Sort.Direction.ASC, sortList.get(1).direction());
    assertEquals("field1", sortList.get(1).field());
  }

  @Test
  void testSorterParseSort_WithIllegalField() {
    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> sorter.parseSort("illegalField:DESC"));

    assertEquals("Cannot sort on 'illegalField'", returnedException.getMessage());
  }

  @Test
  void testSorterParseSort_WithIllegalDirection() {
    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> sorter.parseSort("field1:ILLEGALDIRECTION"));

    assertEquals("'ILLEGALDIRECTION' is not a valid direction", returnedException.getMessage());
  }

  @Test
  void testSorterParseSort_WithObjectSpec() {
    MockSpec mockSpec = new MockSpec("test", "test", false, 10, new MockNestedSpec("test", 2));
    Sorter sorter = new Sorter(Collections.singletonList(new Cursor.SortBy(Sort.Direction.ASC, "dummyString1")), mockSpec);
    List<Cursor.SortBy> sortList = sorter.parseSort("dummyString2:DESC,dummyNumber:ASC");
    assertEquals(2, sortList.size());
    assertEquals(Sort.Direction.DESC, sortList.get(0).direction());
    assertEquals("dummyString2", sortList.get(0).field());
    assertEquals(Sort.Direction.ASC, sortList.get(1).direction());
    assertEquals("dummyNumber", sortList.get(1).field());
  }

  @Test
  void testSorterParseSort_WithClassSpec() {
    Sorter sorter = new Sorter(Collections.singletonList(new Cursor.SortBy(Sort.Direction.ASC, "dummyString1")), MockSpec.class);
    List<Cursor.SortBy> sortList = sorter.parseSort("dummyString2:DESC,dummyNumber:ASC");
    assertEquals(2, sortList.size());
    assertEquals(Sort.Direction.DESC, sortList.get(0).direction());
    assertEquals("dummyString2", sortList.get(0).field());
    assertEquals(Sort.Direction.ASC, sortList.get(1).direction());
    assertEquals("dummyNumber", sortList.get(1).field());
  }

  @Value
  private static class MockSpec {
    String dummyString1;
    String dummyString2;
    Boolean dummyBoolean;
    Integer dummyNumber;
    MockNestedSpec dummyNestedSpec;
  }

  @Value
  private static class MockNestedSpec {
    String nestedString;
    Integer nestedInteger;
  }
}
