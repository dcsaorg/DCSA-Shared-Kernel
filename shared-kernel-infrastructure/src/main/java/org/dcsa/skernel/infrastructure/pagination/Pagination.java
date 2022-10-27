package org.dcsa.skernel.infrastructure.pagination;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.infrastructure.http.queryparams.ComparisonType;
import org.dcsa.skernel.infrastructure.sorting.Sorter.SortableFields;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 *      .filterParameters(filterParameters)
 *      .paginate(pageRequest -> service.find(pageRequest, otherSearchParameters, ...));
 * </pre></code>
 * </p>
 */
@UtilityClass
public class Pagination {
  public static final String DCSA_PAGE_PARAM_NAME = "page";
  public static final String DCSA_PAGESIZE_PARAM_NAME = "limit";
  public static final String DCSA_SORT_PARAM_NAME = "sort";
  public static final PagingNames DCSA_PAGING_NAMES = PagingNames.of(DCSA_PAGE_PARAM_NAME, DCSA_PAGESIZE_PARAM_NAME, DCSA_SORT_PARAM_NAME);

  /**
   * Starts pagination using the standard DCSA parameter naming using "limit" in place of "pageSize".
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
  public interface AcceptsFilterParameters extends DoPaginate {
    /**
     * Sets filter on the parameter names as to only include supported parameters in paging URLs.
     * If not specified the default is FilterParameters.allowAny().
     */
    DoPaginate filterParameters(FilterParameters filterParameters);
  }
  public interface AcceptSortParameters {
    /**
     * Sets the sort to the specified order.
     */
    AcceptsFilterParameters sortBy(List<Sort.Order> defaultSort);

    /**
     * Sets the sort to the order specified in "sort" and defaulting to "defaultSort" if "sort" is null.
     * The incoming "sort" parameter is parsed with the format of 'field:direction' e.g. date:ASC or e.g.
     * date:ASC,reference:DESC.
     */
    AcceptsFilterParameters sortBy(String sort, List<Sort.Order> defaultSort, SortableFields sortableFields);
  }

  @Slf4j
  @RequiredArgsConstructor
  private static class Paginator implements AcceptSortParameters, AcceptsFilterParameters, DoPaginate {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final int page;
    private final int pageSize;
    private final PagingNames pagingNames;
    private List<Sort.Order> sort;
    private Set<String> pageSizeAndSortParams;
    private FilterParameters filterParameters = FilterParameters.allowAny();

    @Override
    public AcceptsFilterParameters sortBy(List<Order> defaultSort) {
      this.sort = defaultSort;
      this.pageSizeAndSortParams = pagingNames.pageSizeParams;
      return this;
    }

    @Override
    public AcceptsFilterParameters sortBy(String sort, List<Order> defaultSort, SortableFields sortableFields) {
      this.sort = parseSort(sort, defaultSort, sortableFields);
      this.pageSizeAndSortParams = pagingNames.pageSizeAndSortParams;
      return this;
    }

    @Override
    public DoPaginate filterParameters(FilterParameters filterParameters) {
      this.filterParameters = filterParameters;
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
        String firstValue = (values != null && values.length > 0) ? values[0] : "";
        if (!pagingNames.pageName.equals(key) && (isValidPageSizeAndSortParam(key) || filterParameters.isValidFilterParam(key))) {
          parameters.append("&").append(urlEncode(key)).append("=").append(urlEncode(firstValue));
        } else if (!pagingNames.pageName.equals(key)) {
          log.warn("Ignored request parameter '{}' = '{}'", key, firstValue);
        }
      });

      return parameters.toString();
    }

    private boolean isValidPageSizeAndSortParam(String key) {
      return pageSizeAndSortParams.contains(key);
    }

    private String urlEncode(String src) {
      return URLEncoder.encode(src, Charset.defaultCharset());
    }
  }

  public record PagingNames(String pageName, Set<String> pageSizeAndSortParams, Set<String> pageSizeParams) {
    public PagingNames {
      assert pageName != null && !pageName.isEmpty();
      assert pageSizeAndSortParams.size() == 2;
      assert pageSizeParams.size() == 1;
    }

    public static PagingNames of(String pageName, String pageSizeName, String sortName) {
      assert pageSizeName != null && !pageSizeName.isEmpty();
      assert sortName != null && !sortName.isEmpty();
      return new PagingNames(pageName, Set.of(pageSizeName, sortName), Set.of(pageSizeName));
    }
  }

  public record FilterParameters(Set<String> filterParameters) {
    /**
     * Matches any parameter.
     */
    public static FilterParameters allowAny() {
      return new FilterParameters(null);
    }

    /**
     * Matches no parameters.
     */
    public static FilterParameters allowNone() {
      return new FilterParameters(Collections.emptySet());
    }

    public static FilterParameters allow(Set<String> filterParameters) {
      return new FilterParameters(filterParameters);
    }

    public static FilterParameters allow(String... filterParameters) {
      return new FilterParameters(Set.of(filterParameters));
    }

    /**
     * Matches parameters to the names of the fields in the given type.
     * @param filterParamsType a type whose fields are named the same as the allowed filter parameters.
     */
    public static FilterParameters allow(Class<?> filterParamsType) {
      return new FilterParameters(getFieldNamesFromClass(filterParamsType));
    }

    boolean isValidFilterParam(String key) {
      if (filterParameters == null || filterParameters.contains(key)) {
        return true;
      }

      String[] keyParts = key.split(":");
      if (keyParts.length == 2) {
        return filterParameters.contains(keyParts[0]) && ComparisonType.VALUE_SET.contains(keyParts[1].toUpperCase());
      } else {
        return false;
      }
    }

    /**
     * filterParamsType is generally assumed a flat set of values from the http request object so
     * there is no need to look recursively.
     */
    private static Set<String> getFieldNamesFromClass(Class<?> filterParamsType) {
      return addFieldNamesFromClass(new HashSet<>(), filterParamsType);
    }

    private static Set<String> addFieldNamesFromClass(Set<String> fieldNames, Class<?> filterParamsType) {
      if (filterParamsType == null || Object.class.equals(filterParamsType)) {
        return fieldNames;
      }

      Arrays.stream(filterParamsType.getDeclaredFields())
        .filter(field -> (field.getModifiers() & Modifier.STATIC) == 0)
        .map(Field::getName)
        .forEach(fieldNames::add);

      return addFieldNamesFromClass(fieldNames, filterParamsType.getSuperclass());
    }
  }
}
