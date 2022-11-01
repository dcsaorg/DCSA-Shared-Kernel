package org.dcsa.skernel.infrastructure.sorting;

import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.infrastructure.pagination.Cursor;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Helper class for transforming the DCSA Sorting syntax to Spring-data Sorting. */
public class Sorter {
  /**
   * <p>Example:
   * <code><pre>
   *    public final SortableFields sortableFields =
   *      SortableFields
   *        .of("name", otherfields, ...)
   *        .addMapping("shippingInstructionCreatedDateTime", "createdDateTime");
   * </pre></code>
   * </p>
   */
  public static class SortableFields {
    private final Map<String, String> sortableFields;

    private SortableFields(Map<String, String> sortableFields) {
      Objects.requireNonNull(sortableFields);
      assert !sortableFields.isEmpty();
      this.sortableFields = sortableFields;
    }

    public static SortableFields of(Set<String> sortableFields) {
      return new SortableFields(
        sortableFields.stream().collect(Collectors.toMap(Function.identity(), Function.identity()))
      );
    }

    public static SortableFields of(String... sortableFields) {
      return new SortableFields(
        Arrays.stream(sortableFields).collect(Collectors.toMap(Function.identity(), Function.identity()))
      );
    }

    /**
     * Adds or overrides a mapping from an input name to a database entity field.
     */
    public SortableFields addMapping(String k, String v) {
      sortableFields.put(k, v);
      return this;
    }
  }

  private final List<Cursor.SortBy> defaultSort;
  private final Map<String, String> sortableFields;

  @Deprecated
  public Sorter(List<Cursor.SortBy> defaultSort, String... sortableFields) {
    this.defaultSort = defaultSort;
    this.sortableFields = Arrays.stream(sortableFields).collect(Collectors.toMap(Function.identity(), Function.identity()));
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
    return parseSort(sort, defaultSort, sortableFields, Cursor.SortBy::new, Cursor.SortBy::field);
  }

  /**
   * Parses the incoming sort parameter with the format of 'field:direction' e.g. date:ASC or e.g.
   * date:ASC,reference:DESC into a Sort.Order used by Spring.
   *
   * Note the defaultSort is assumed to contain at least one field that is reasonable unique as to give a stable
   * result.
   */
  public static List<Sort.Order> parseSort(String sort, List<Sort.Order> defaultSort, SortableFields sortableFields) {
    return parseSort(sort, defaultSort, sortableFields.sortableFields, Sort.Order::new, Sort.Order::getProperty);
  }

  private static <T> List<T> parseSort(String sort, List<T> defaultSort, Map<String, String> sortableFields,
                                       BiFunction<Sort.Direction, String, T> creator, Function<T, String> getField) {
    if (sort == null) {
      return defaultSort;
    }

    Set<String> addedFields = new HashSet();
    List<T> result = Arrays.stream(sort.split(","))
      .map(String::trim)
      .map(
        sortField -> {
          String[] fieldAndDirection = sortField.split(":", 2);
          String actualSortField = fieldAndDirection[0];
          Sort.Direction direction = Sort.Direction.ASC;
          assert fieldAndDirection.length <= 2;

          if (!sortableFields.containsKey(actualSortField)) {
            throw ConcreteRequestErrorMessageException.invalidQuery(
              "sort", "Cannot sort on '" + actualSortField
                + "'. This implementation supports the following sortable fields: "
                + sortableFields.keySet().stream().sorted().collect(Collectors.joining(", ")));
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

          String dbField = sortableFields.get(actualSortField);
          addedFields.add(dbField);
          return creator.apply(direction, dbField);
        })
      .collect(Collectors.toCollection(ArrayList::new));

    // Assuming that the defaultSort is on something reasonable unique that gives a stable result
    // always add the fields from defaultSort to the end if they are not already used. This ensures
    // a stable result even if the user decides to sort on something that is not unique.
    defaultSort.stream()
      .filter(o -> !addedFields.contains(getField.apply(o)))
      .forEach(result::add);

    return result;
  }
}
