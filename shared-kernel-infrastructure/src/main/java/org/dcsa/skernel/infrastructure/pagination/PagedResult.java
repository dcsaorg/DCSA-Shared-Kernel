package org.dcsa.skernel.infrastructure.pagination;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Result from a paged method.
 */
public record PagedResult<T>(
  int totalPages,
  List<T> content
) {
  /**
   * Constructs a PagedResult from the given page
   */
  public PagedResult(Page<T> page) {
    this(page.getTotalPages(), page.getContent());
  }

  /**
   * Constructs a PagedResult from the given page with the contents mapped using the given mapper.
   */
  public <D> PagedResult(Page<D> page, Function<D, T> mapper) {
    this(page.getTotalPages(), page.getContent().stream().map(mapper).toList());
  }
}
