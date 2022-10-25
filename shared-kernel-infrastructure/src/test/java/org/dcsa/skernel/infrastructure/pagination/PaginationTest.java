package org.dcsa.skernel.infrastructure.pagination;

import org.dcsa.skernel.infrastructure.pagination.Pagination.FilterParameters;
import org.dcsa.skernel.infrastructure.pagination.Pagination.PagingNames;
import org.dcsa.skernel.infrastructure.sorting.Sorter.SortableFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaginationTest {
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);

  private final List<Sort.Order> defaultSort = List.of(new Sort.Order(Direction.ASC, "created_timestamp"));

  @BeforeEach
  public void resetMocks() {
    reset(request, response);

    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(9090);
    when(request.getRequestURI()).thenReturn("/vx/myService");
  }

  @Test
  public void testSetPageHeaders_Page0Of1() {
    // Execute
    Pagination
      .with(request, response, 0, 10)
      .sortBy(defaultSort)
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 1))
      );

    // Verify
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?page=0");
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?page=0");
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?page=0");
    verify(response, never()).setHeader(eq("Next-Page"), any());
    verify(response, never()).setHeader(eq("Previous-Page"), any());
  }

  @Test
  public void testSetPageHeaders_Page0Of2() {
    // Execute
    Pagination
      .with(request, response, 0, 10)
      .sortBy(defaultSort)
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 2))
      );

    // Verify
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?page=0");
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?page=0");
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?page=1");
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?page=1");
    verify(response, never()).setHeader(eq("Previous-Page"), any());
  }

  @Test
  public void testSetPageHeaders_Page1Of2() {
    // Execute
    Pagination
      .with(request, response, 1, 10)
      .sortBy(defaultSort)
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 2))
      );

    // Verify
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?page=1");
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?page=0");
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?page=1");
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?page=0");
    verify(response, never()).setHeader(eq("Next-Page"), any());
  }

  @Test
  public void testSetPageHeaders_Page1Of3() {
    // Execute
    Pagination
      .with(request, response, 1, 10)
      .sortBy(defaultSort)
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 3))
      );

    // Verify
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?page=1");
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?page=0");
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?page=2");
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?page=2");
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?page=0");
  }

  @Test
  public void testSetPageHeaders_AllParametersAllowed() {
    // Setup
    when(request.getParameterMap()).thenReturn(
      sortedMapOf("carrierBookingReference", "cbr-12424534", "otherStuff", "testValue"));

    // Execute
    Pagination
      .with(request, response, 1, 10)
      .sortBy(defaultSort)
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 3))
      );

    // Verify
    String parameters = "&carrierBookingReference=cbr-12424534&otherStuff=testValue";
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?page=1" + parameters);
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
  }

  @Test
  public void testSetPageHeaders_NoParametersAllowed_NoSort_LimitAlwaysAllowed_MultiplePageIsIgnored() {
    // Setup
    when(request.getParameterMap()).thenReturn(
      sortedMapOf(
        "carrierBookingReference", "cbr-12424534",
        "limit", "2",
        "page", "12345",
        "otherStuff", "testValue",
        "sort", "createdDateTime"
      ));

    // Execute
    Pagination
      .with(request, response, 1, 10)
      .sortBy(defaultSort)
      .filterParameters(FilterParameters.allowNone())
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 3))
      );

    // Verify
    String parameters = "&limit=2";
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?page=1" + parameters);
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
  }

  @Test
  public void testSetPageHeaders_NoParametersAllowedAndWithSort() {
    // Setup
    when(request.getParameterMap()).thenReturn(
      sortedMapOf("carrierBookingReference", "cbr-12424534", "otherStuff", "testValue", "sort", "createdDateTime:asc"));

    // Execute
    Pagination
      .with(request, response, 1, 10)
      .sortBy("createdDateTime:asc", defaultSort, SortableFields.of("createdDateTime"))
      .filterParameters(FilterParameters.allowNone())
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 3))
      );

    // Verify
    String parameters = "&sort=createdDateTime%3Aasc";
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?page=1" + parameters);
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
  }

  @Test
  public void testSetPageHeaders_CertainParametersAllowed() {
    // Setup
    when(request.getParameterMap()).thenReturn(
      sortedMapOf("carrierBookingReference", "cbr-12424534", "otherStuff", "testValue"));

    // Execute
    Pagination
      .with(request, response, 1, 10)
      .sortBy(defaultSort)
      .filterParameters(FilterParameters.allow("carrierBookingReference"))
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 3))
      );

    // Verify
    String parameters = "&carrierBookingReference=cbr-12424534";
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?page=1" + parameters);
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
  }

  @Test
  public void testSetPageHeaders_CertainParametersAllowedByClass() {
    // Setup
    when(request.getParameterMap()).thenReturn(
      sortedMapOf(
        "carrierBookingReference", "cbr-12424534",
        "vesselIMONumber", "vimon2345",
        "otherStuff", "testValue"
      ));

    // Execute
    Pagination
      .with(request, response, 1, 10)
      .sortBy(defaultSort)
      .filterParameters(FilterParameters.allow(TestFilterParameters.class))
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 3))
      );

    // Verify
    String parameters = "&carrierBookingReference=cbr-12424534&vesselIMONumber=vimon2345";
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?page=1" + parameters);
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?page=2" + parameters);
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?page=0" + parameters);
  }
  public record TestFilterParameters(String carrierBookingReference, String vesselIMONumber) { }

  @Test
  public void testSetPageHeaders_CustomPagingNames() {
    // Setup
    when(request.getParameterMap()).thenReturn(
      sortedMapOf(
        "sizzle", "45",
        "soffle", "createdDateTime",
        "otherStuff", "testValue"
      ));

    // Execute
    Pagination
      .with(request, response, 1, 10, PagingNames.of("poff", "sizzle", "soffle"))
      .sortBy("createdDateTime", defaultSort, SortableFields.of("createdDateTime"))
      .filterParameters(FilterParameters.allowNone())
      .paginate(pageRequest ->
        new PagedResult<>(new PageImpl<>(List.of(""), Pageable.unpaged(), 3))
      );

    // Verify
    String parameters = "&sizzle=45&soffle=createdDateTime";
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?poff=1" + parameters);
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?poff=0" + parameters);
    verify(response).setHeader("Last-Page", "http://localhost:9090/vx/myService?poff=2" + parameters);
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?poff=2" + parameters);
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?poff=0" + parameters);
  }

  // With a sorted map we know the order of elements
  private SortedMap<String, String[]> sortedMapOf(String... s) {
    SortedMap<String, String[]> m = new TreeMap<>();
    for (int i = 0; i < s.length; i += 2) {
      m.put(s[i], new String[] { s[i + 1] });
    }
    return m;
  }
}
