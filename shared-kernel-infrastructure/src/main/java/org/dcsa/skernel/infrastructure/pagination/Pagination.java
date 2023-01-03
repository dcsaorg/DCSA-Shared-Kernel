package org.dcsa.skernel.infrastructure.pagination;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.dcsa.skernel.infrastructure.sorting.Sorter.SortableFields;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;

import static org.dcsa.skernel.infrastructure.sorting.Sorter.parseSort;

/**
 * A helper class for Spring's page/offset based pagination and the DCSA pagination headers.
 *
 * <p>Usage:
 * <code><pre>
 *    return Pagination
 *      .with(request, response, page, pageSize)
 *      .sortBy(sort, defaultSort, sortableFields)
 *      .paginate(pageRequest -> service.find(pageRequest, otherSearchParameters, ...));
 * </pre></code>
 * </p>
 */
@UtilityClass
public class Pagination {
  public static final String DCSA_PAGE_PARAM_NAME = "page";
  public static final String DCSA_PAGESIZE_PARAM_NAME = "limit";
  public static final String DCSA_SORT_PARAM_NAME = "sort";
  public static final PagingNames DCSA_PAGING_NAMES = PagingNames.of(DCSA_PAGE_PARAM_NAME);

  /**
   * Starts pagination using the default parameter naming.
   */
  public static AcceptSortParameters with(HttpServletRequest request, HttpServletResponse response, int page, int pageSize) {
    return new Paginator(request, response, page, pageSize, DCSA_PAGING_NAMES);
  }

  /**
   * Starts pagination using the paging with names you provide.
   */
  public static AcceptSortParameters with(HttpServletRequest request, HttpServletResponse response, int page, int pageSize, PagingNames pagingNames) {
    return new Paginator(request, response, page, pageSize, pagingNames);
  }

  public interface DoPaginate {
    /**
     * Performs pagination on the result provided from the function.
     */
    <T> List<T> paginate(Function<PageRequest, PagedResult<T>> func);
  }
  public interface AcceptSortParameters {
    /**
     * <p>Sets the sort to the specified order.</p>
     *
     * <p><b>Note the defaultSort is assumed to contain at least one field that is reasonable unique as to give a stable
     * result.</b></p>
     */
    DoPaginate sortBy(List<Sort.Order> defaultSort);

    /**
     * <p>Sets the sort to the order specified in "sort" and defaulting to "defaultSort" if "sort" is null.
     * The incoming "sort" parameter is parsed with the format of 'field:direction' e.g. date:ASC or e.g.
     * date:ASC,reference:DESC.</p>
     *
     * <p><b>Note the defaultSort is assumed to contain at least one field that is reasonable unique as to give a stable
     * result.</b></p>
     */
    DoPaginate sortBy(String sort, List<Sort.Order> defaultSort, SortableFields sortableFields);
  }

  @RequiredArgsConstructor
  private static class Paginator implements AcceptSortParameters, DoPaginate {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final int page;
    private final int pageSize;
    private final PagingNames pagingNames;
    private List<Sort.Order> sort;

    @Override
    public DoPaginate sortBy(List<Order> defaultSort) {
      this.sort = defaultSort;
      return this;
    }

    @Override
    public DoPaginate sortBy(String sort, List<Order> defaultSort, SortableFields sortableFields) {
      this.sort = parseSort(sort, defaultSort, sortableFields);
      return this;
    }

    @Override
    public <T> List<T> paginate(Function<PageRequest, PagedResult<T>> func) {
      PagedResult<T> pagedResult = func.apply(PageRequest.of(page, pageSize, Sort.by(sort)));
      setPaginationHeaders(pagedResult);
      return pagedResult.content();
    }

    private void setPaginationHeaders(PagedResult<?> pagedResult) {
      String basePath = request.getScheme() + "://" +
        request.getServerName() + ":" + request.getServerPort() +
        request.getRequestURI() + "?" + pagingNames.pageName+ "=";
      String parameters = getRequestParameters();

      response.setHeader("Current-Page", basePath + page + parameters);
      response.setHeader("First-Page", basePath + "0" + parameters);
      response.setHeader("Last-Page", basePath + Math.max(0, pagedResult.totalPages() - 1) + parameters);

      if (page > 0) {
        response.setHeader("Previous-Page", basePath + (page - 1) + parameters);
      }
      if (pagedResult.totalPages() > page + 1) {
        response.setHeader("Next-Page", basePath + (page + 1) + parameters);
      }
    }

    private String getRequestParameters() {
      StringBuilder parameters = new StringBuilder();

      request.getParameterMap().forEach((String key, String[] values) -> {
        if (!pagingNames.pageName.equals(key)) {
          String firstValue = (values != null && values.length > 0) ? values[0] : "";
          parameters.append("&").append(urlEncode(key)).append("=").append(urlEncode(firstValue));
        }
      });

      return parameters.toString();
    }

    private String urlEncode(String src) {
      return URLEncoder.encode(src, Charset.defaultCharset());
    }
  }

  public record PagingNames(String pageName) {
    public PagingNames {
      assert pageName != null && !pageName.isEmpty();
    }

    public static PagingNames of(String pageName) {
      return new PagingNames(pageName);
    }
  }
}
