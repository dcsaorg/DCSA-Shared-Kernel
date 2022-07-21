package org.dcsa.skernel.errors.infrastructure;

import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConcreteRequestErrorMessageExceptionHandlerTest extends BaseExceptionHandlerTest {
  @Test
  @DisplayName("Test for not found exception")
  void testNotFound() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(ConcreteRequestErrorMessageException.notFound("You can't find me."));

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("notFound"))
      .andExpect(jsonPath("$.errors[0].message").value("You can't find me."));
  }

  @Test
  @DisplayName("Test for invalid input exception")
  void testInvalidInput() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(
      ConcreteRequestErrorMessageException.invalidInput(
        "Invalid input",
        new IllegalArgumentException()));

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
      .andExpect(jsonPath("$.errors[0].message").value("Invalid input"));
  }

  @Test
  @DisplayName("Test for internal server error")
  void testInternalServerError() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(
      ConcreteRequestErrorMessageException.internalServerError("Internal server error"));

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("internalError"))
      .andExpect(jsonPath("$.errors[0].message").value("Internal server error"));

  }

  protected Object controllerAdvice() {
    return new ConcreteRequestErrorMessageExceptionHandler();
  }
}
