package org.dcsa.skernel.errors.infrastructure;

import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ServerWebInputException;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JakartaValidationExceptionHandlerTest extends BaseExceptionHandlerTest {
  @Test
  @DisplayName("Test constraint violation with invalid input")
  void testConstraintViolationExceptionInputParameter() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(validateObject());

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
      .andExpect(jsonPath("$.errors[0].message").value(anyOf(
        equalTo("limitedField size must be between 0 and 5;requiredField must not be null"),
        equalTo("requiredField must not be null;limitedField size must be between 0 and 5")
      )));
  }

  @Test
  @DisplayName("Test constraint violation as cause of ServerWebInputException")
  void testServerWebInputException() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(new ServerWebInputException("", null, validateObject()));

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
      .andExpect(jsonPath("$.errors[0].message").value(anyOf(
        equalTo("limitedField size must be between 0 and 5;requiredField must not be null"),
        equalTo("requiredField must not be null;limitedField size must be between 0 and 5")
      )));
  }

  private Exception validateObject() {
    ValidationObject testObject = new ValidationObject();
    testObject.setLimitedField("123456");

    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      Validator validator = factory.getValidator();
      return new ConstraintViolationException(validator.validate(testObject));
    }
  }

  @Data
  static class ValidationObject {
    @NotNull
    private String requiredField;

    @Size(max = 5)
    private String limitedField;
  }

  protected Object controllerAdvice() {
    return new JakartaValidationExceptionHandler();
  }
}
