package org.dcsa.skernel.infrastructure.pagination;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.dcsa.skernel.infrastructure.pagination.Cursor.SortBy;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defaults if no other options are given.
 */
@Deprecated
@Value
@RequiredArgsConstructor
public class CursorDefaults {
  private final int pageSize;

  @NonNull
  private final List<SortBy> sortBy;

  public CursorDefaults(int pageSize, Cursor.SortBy... sortBy) {
    this(pageSize, Arrays.asList(sortBy));
  }

  /**
   * Constructor to use the same direction on all fields.
   */
  public CursorDefaults(int pageSize, Sort.Direction direction, String... fields) {
    this(
      pageSize,
      Arrays.stream(fields).map(field -> new Cursor.SortBy(direction, field)).collect(Collectors.toList())
    );
  }
}
