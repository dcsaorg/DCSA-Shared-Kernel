package org.dcsa.skernel.infrastructure.sorting;

import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.infrastructure.pagination.Cursor;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** Helper class for transforming the DCSA Sorting syntax to Spring-data Sorting. */
public class Sorter {

  private final List<Cursor.SortBy> defaultSort;
  private final Set<String> sortableFields;

  public Sorter(List<Cursor.SortBy> defaultSort, String... sortableFields) {
    this.defaultSort = defaultSort;
    this.sortableFields = Set.of(sortableFields);
  }

  /**
   * Parses the incoming sort parameter with the format of 'field:direction' e.g. date:ASC or e.g.
   * date:ASC,reference:DESC into a CursorSortBy used by the Paginator class.
   */
  public List<Cursor.SortBy> parseSort(String sort) {
    if (sort == null) {
      return defaultSort;
    }

    return Arrays.stream(sort.split(","))
        .map(String::trim)
        .map(
            sortField -> {
              String[] fieldAndDirection = sortField.split(":");
              String actualSortField = fieldAndDirection[0];
              Sort.Direction direction = Sort.Direction.ASC;

              if (!sortableFields.contains(actualSortField)) {
                throw ConcreteRequestErrorMessageException.invalidQuery(
                    "sort", "Cannot sort on '" + actualSortField + "'");
              }

              if (fieldAndDirection.length == 2) {
                direction =
                    Sort.Direction.fromOptionalString(fieldAndDirection[1])
                        .orElseThrow(
                            () ->
                                ConcreteRequestErrorMessageException.invalidQuery(
                                    "sort",
                                    "'" + fieldAndDirection[1] + "' is not a valid direction"));
              } else if (fieldAndDirection.length > 2) {
                throw ConcreteRequestErrorMessageException.invalidQuery(
                    "sort", "'" + sortField + "' is not valid");
              }

              return new Cursor.SortBy(direction, actualSortField);
            })
        .toList();
  }
}
