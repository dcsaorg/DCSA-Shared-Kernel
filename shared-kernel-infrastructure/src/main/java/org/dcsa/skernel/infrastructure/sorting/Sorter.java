package org.dcsa.skernel.infrastructure.sorting;

import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.infrastructure.pagination.Cursor;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/** Helper class for transforming the DCSA Sorting syntax to Spring-data Sorting. */
public class Sorter {
  public record SortableFields(Set<String> sortableFields) {
    public SortableFields {
      Objects.requireNonNull(sortableFields);
      assert !sortableFields.isEmpty();
    }

    public static SortableFields of(Set<String> sortableFields) {
      return new SortableFields(sortableFields);
    }

    public static SortableFields of(String... sortableFields) {
      return new SortableFields(Set.of(sortableFields));
    }
  }

  private final List<Cursor.SortBy> defaultSort;
  private final Set<String> sortableFields;

  @Deprecated
  public Sorter(List<Cursor.SortBy> defaultSort, String... sortableFields) {
    this.defaultSort = defaultSort;
    this.sortableFields = Set.of(sortableFields);
    if (this.sortableFields.isEmpty()) {
      throw new IllegalArgumentException("Must allow at least one sortable field!");
    }
  }

  /**
   * Parses the incoming sort parameter with the format of 'field:direction' e.g. date:ASC or e.g.
   * date:ASC,reference:DESC into a CursorSortBy used by the Paginator class.
   */
  @Deprecated
  public List<Cursor.SortBy> parseSort(String sort) {
    return parseSort(sort, defaultSort, sortableFields, Cursor.SortBy::new);
  }

  /**
   * Parses the incoming sort parameter with the format of 'field:direction' e.g. date:ASC or e.g.
   * date:ASC,reference:DESC into a Sort.Order used by Spring.
   */
  public static List<Sort.Order> parseSort(String sort, List<Sort.Order> defaultSort, SortableFields sortableFields) {
    return parseSort(sort, defaultSort, sortableFields.sortableFields, Sort.Order::new);
  }

  private static <T> List<T> parseSort(String sort, List<T> defaultSort, Set<String> sortableFields, BiFunction<Sort.Direction, String, T> creator) {
    if (sort == null) {
      return defaultSort;
    }

    return Arrays.stream(sort.split(","))
      .map(String::trim)
      .map(
        sortField -> {
          String[] fieldAndDirection = sortField.split(":", 2);
          String actualSortField = fieldAndDirection[0];
          Sort.Direction direction = Sort.Direction.ASC;
          assert fieldAndDirection.length <= 2;

          if (!sortableFields.contains(actualSortField)) {
            throw ConcreteRequestErrorMessageException.invalidQuery(
              "sort", "Cannot sort on '" + actualSortField
                + "'. This implementation supports the following sortable fields: "
                + sortableFields.stream().sorted().collect(Collectors.joining(", ")));
          }

          if (fieldAndDirection.length == 2) {
            direction =
              Sort.Direction.fromOptionalString(fieldAndDirection[1])
                .orElseThrow(
                  () ->
                    ConcreteRequestErrorMessageException.invalidQuery(
                      "sort",
                      "'" + fieldAndDirection[1] + "' is not a valid direction."
                        + " Please use ASC or DESC as direction."));
          }

          return creator.apply(direction, actualSortField);
        })
      .toList();
  }
}
