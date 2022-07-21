package org.dcsa.skernel.errors.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FallbackExceptionHandlerTest extends BaseExceptionHandlerTest {
  @Test
  public void testGenericException() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(new Exception("generic exception"));

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("internalError"))
      .andExpect(jsonPath("$.errors[0].message").value("Internal error"));
  }

  @Test
  @DisplayName("Test for a ResponseStatusException")
  void testResponseStatusException() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("internalError"))
      .andExpect(jsonPath("$.errors[0].message").value("Internal error"));
  }

  protected Object controllerAdvice() {
    return new FallbackExceptionHandler();
  }
}
