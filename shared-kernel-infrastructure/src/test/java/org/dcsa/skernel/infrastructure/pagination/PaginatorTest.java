package org.dcsa.skernel.infrastructure.pagination;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaginatorTest {
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final CursorDefaults cursorDefaults = new CursorDefaults(10, Sort.Direction.ASC, "sort_field");

  private final Paginator paginator = new Paginator(new ObjectMapper()
    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .findAndRegisterModules());

  @BeforeEach
  public void resetMocks() {
    reset(request, response);
  }

  @Test
  public void testSerialization() {
    Cursor original = new Cursor(2, 20, Sort.Direction.ASC, "created_timestamp");

    String serialized = paginator.cursorToString(original);
    Cursor deserialized = paginator.cursorFromString(serialized);

    assertEquals(original, deserialized);
  }

  @Test
  public void testParseRequest_WithCursor() {
    // Setup
    Cursor original = new Cursor(2, 20, Sort.Direction.ASC, "created_timestamp");
    when(request.getParameter("cursor")).thenReturn(paginator.cursorToString(original));

    // Execute
    Cursor cursor = paginator.parseRequest(request, cursorDefaults);

    // Verify
    assertEquals(original, cursor);
  }

  @Test
  public void testParseRequest_WithLimit() {
    // Setup
    when(request.getParameter("limit")).thenReturn("33");

    // Execute
    Cursor cursor = paginator.parseRequest(request, cursorDefaults);

    // Verify
    assertEquals(0, cursor.getPage());
    assertEquals(33, cursor.getPageSize());
    assertEquals(cursorDefaults.getSortBy(), cursor.getSortBy());
  }

  @Test
  public void testParseRequest_NoArgs() {
    // Execute
    Cursor cursor = paginator.parseRequest(request, cursorDefaults);

    // Verify
    assertEquals(0, cursor.getPage());
    assertEquals(cursorDefaults.getPageSize(), cursor.getPageSize());
    assertEquals(cursorDefaults.getSortBy(), cursor.getSortBy());
  }

  @Test
  public void testSetPageHeaders_Page0Of1() {
    // Setup
    Cursor cursor = new Cursor(0, 20, Sort.Direction.ASC, "created_timestamp");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(9090);
    when(request.getRequestURI()).thenReturn("/vx/myService");

    // Execute
    paginator.setPageHeaders(request, response, cursor, 1);

    // Verify
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor));
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor));
    verify(response, never()).setHeader(eq("Next-Page"), any());
    verify(response, never()).setHeader(eq("Previous-Page"), any());
  }

  /* */
  @Test
  public void testSetPageHeaders_Page0Of2() {
    // Setup
    Cursor cursor = new Cursor(0, 20, Sort.Direction.ASC, "created_timestamp");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(9090);
    when(request.getRequestURI()).thenReturn("/vx/myService");

    // Execute
    paginator.setPageHeaders(request, response, cursor, 2);

    // Verify
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor));
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor));
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(1)));
    verify(response, never()).setHeader(eq("Previous-Page"), any());
  }

  @Test
  public void testSetPageHeaders_Page1Of2() {
    // Setup
    Cursor cursor = new Cursor(1, 20, Sort.Direction.ASC, "created_timestamp");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(9090);
    when(request.getRequestURI()).thenReturn("/vx/myService");

    // Execute
    paginator.setPageHeaders(request, response, cursor, 2);

    // Verify
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(1)));
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(0)));
    verify(response, never()).setHeader(eq("Next-Page"), any());
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(0)));
  }

  @Test
  public void testSetPageHeaders_Page1Of3() {
    // Setup
    Cursor cursor = new Cursor(1, 20, Sort.Direction.ASC, "created_timestamp");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(9090);
    when(request.getRequestURI()).thenReturn("/vx/myService");

    // Execute
    paginator.setPageHeaders(request, response, cursor, 3);

    // Verify
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(1)));
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(0)));
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(2)));
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(0)));
  }

  @Test
  public void testSetPageHeaders_AllParametersAllowed() {
    // Setup
    Cursor cursor = new Cursor(1, 20, Sort.Direction.ASC, "created_timestamp");
    when(request.getParameterMap()).thenReturn(Map.of("carrierBookingReference", new String[]{"cbr-12424534"}, "otherStuff", new String[]{"testValue"}));
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(9090);
    when(request.getRequestURI()).thenReturn("/vx/myService");

    // Execute
    paginator.setPageHeaders(request, response, cursor, 3);

    // Verify
    verifyPageHeader("Current-Page", cursor.withPage(1), "&carrierBookingReference=cbr-12424534", "&otherStuff=testValue");
    verifyPageHeader("First-Page", cursor.withPage(0), "&carrierBookingReference=cbr-12424534", "&otherStuff=testValue");
    verifyPageHeader("Next-Page", cursor.withPage(2), "&carrierBookingReference=cbr-12424534", "&otherStuff=testValue");
    verifyPageHeader("Previous-Page", cursor.withPage(0), "&carrierBookingReference=cbr-12424534", "&otherStuff=testValue");
  }

  @Test
  public void testSetPageHeaders_WithNotAllowedParameter() {
    // Setup
    Cursor cursor = new Cursor(1, 20, Sort.Direction.ASC, "created_timestamp");
    when(request.getParameterMap()).thenReturn(Map.of("carrierBookingReference", new String[]{"cbr-12424534"}, "otherStuff", new String[]{"testValue"}));
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(9090);
    when(request.getRequestURI()).thenReturn("/vx/myService");

    // Execute
    paginator.setPageHeaders(request, response, cursor, 3, "carrierBookingReference");

    // Verify ('otherStuff' should be filtered out)
    verify(response).setHeader("Current-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(1)) + "&carrierBookingReference=cbr-12424534");
    verify(response).setHeader("First-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(0)) + "&carrierBookingReference=cbr-12424534");
    verify(response).setHeader("Next-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(2)) + "&carrierBookingReference=cbr-12424534");
    verify(response).setHeader("Previous-Page", "http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor.withPage(0)) + "&carrierBookingReference=cbr-12424534");
  }

  /**
   * Because using a map the order of shouldContain in the URL can be random.
   */
  private void verifyPageHeader(String headerName, Cursor cursor, String... shouldContain) {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(response).setHeader(eq(headerName), captor.capture());

    assertTrue(captor.getValue().startsWith("http://localhost:9090/vx/myService?cursor=" + paginator.cursorToString(cursor)));
    for (String str : shouldContain) {
      assertTrue(captor.getValue().contains(str));
    }
  }
}
