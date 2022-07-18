package org.dcsa.skernel.errors.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SpringExceptionHandlerTest extends BaseExceptionHandlerTest {
  @Test
  @DisplayName("Test InvalidDataAccessResourceUsageException")
  void testSqlStateException() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(new InvalidDataAccessResourceUsageException("test message"));

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("internalError"))
      .andExpect(jsonPath("$.errors[0].message").value("Internal error with database operation"));
  }

  @DisplayName("Tests for ServerWebInputException")
  @Nested
  class ServerWebInputExceptionTest {

    @Test
    @DisplayName("Test for ServerWebInputException invalid UUID")
    void testServerWebInputInvalidUUIDException() throws Exception {
      // Setup
      MockMvc mockMvc = setupController(new ServerWebInputException("Invalid UUID string:"));

      // Test and Verify
      mockMvc
        .perform(get("/failWithException"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidParameter"))
        .andExpect(jsonPath("$.errors[0].message").value("Input was not a valid UUID format"));
    }

    @Test
    @DisplayName("Test for generic ServerWebInputException")
    void testServerWebInputException() throws Exception {
      // Setup
      MockMvc mockMvc = setupController(new ServerWebInputException(""));

      // Test and Verify
      mockMvc
        .perform(get("/failWithException"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"));
    }
  }

  @DisplayName("Tests for DecodingException")
  @Nested
  class DecodingExceptionTest {
    @Test
    @DisplayName("Test for flat DecodingException")
    void testDecodingException() throws Exception {
      // Setup
      MockMvc mockMvc = setupController(new ServerWebInputException("DecodingException", null, new DecodingException("")));

      // Test and Verify
      mockMvc
        .perform(get("/failWithException"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"));
    }


    @Test
    @DisplayName("Test for JsonPropertyException as cause of UnrecognizedPropertyException")
    void testDecodingExceptionCausedByUnrecognizedPropertyException() throws Exception {
      // Setup
      MockMvc mockMvc = setupController(parseInvalidJson("{\"TestPoJo\": {\"test\":\"value\"}}"));

      // Test and Verify
      mockMvc
        .perform(get("/failWithException"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
        .andExpect(jsonPath("$.errors[0].message").value(
          "Unrecognized field \"TestPoJo\" , not marked as ignorable at [Source: \"{\"TestPoJo\": "
            + "{\"test\":\"value\"}}\"; line: 1, column: 15]  " +
            "The error is associated with the attribute TestPoJo[\"TestPoJo\"]."));
    }

    @Test
    @DisplayName("Test for InvalidFormatException with invalid Date as cause")
    void testDecodingExceptionCausedByInvalidFormatExceptionDate() throws Exception {
      // Setup
      MockMvc mockMvc = setupController(parseInvalidJson("{\"testString\":\"value\", \"testDate\": \"01-12-2022\"}"));

      // Test and Verify
      mockMvc
        .perform(get("/failWithException"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
        .andExpect(jsonPath("$.errors[0].message").value(
          "Invalid format for date field. " +
            "The value \"01-12-2022\" cannot be parsed via the pattern \"YYYY-MM-DD\". " +
            "Please check the input. The error is associated with the attribute TestPoJo[\"testDate\"]."));
    }

    @Test
    @DisplayName("Test for InvalidFormatException with invalid DateTime as cause")
    void testDecodingExceptionCausedByInvalidFormatExceptionDateTime() throws Exception {
      // Setup
      MockMvc mockMvc = setupController(parseInvalidJson("{\"testString\":\"value\", \"testDateTime\": \"2022-01-23T35:12:00\"}"));

      // Test and Verify
      mockMvc
        .perform(get("/failWithException"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
        .andExpect(jsonPath("$.errors[0].message").value(
          "Invalid format for date time field. The value \"2022-01-23T35:12:00\" " +
            "cannot be parsed via the patterns \"YYYY-MM-DD'T'HH:MM:SS+ZZZZ\" or \"YYYY-MM-DD'T'HH:MM:SS'Z'\". " +
            "Please check the input. The error is associated with the attribute TestPoJo[\"testDateTime\"]."));
    }

    private Exception parseInvalidJson(String json) {
      try {
        objectMapper.readValue(json, TestPoJo.class);
        throw new AssertionError("readValue should have thrown an exception");
      } catch (JsonProcessingException e) {
        return new DecodingException("JsonPropertyException", e);
      }
    }
  }

  @DisplayName("Tests for DataIntegrityViolationException")
  @Nested
  class DataIntegrityViolationExceptionTest {

    @Test
    @DisplayName("DataIntegrityViolationException")
    void testDataIntegrityViolationException() throws Exception {
      // Setup
      MockMvc mockMvc = setupController(new DataIntegrityViolationException("Data needs to have integrity"));

      // Test and Verify
      mockMvc
        .perform(get("/failWithException"))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("conflict"))
        .andExpect(jsonPath("$.errors[0].message").value("Internal error with database operation"));
    }
  }

  @Test
  @DisplayName("MethodArgumentTypeMismatchException")
  void testMethodArgumentTypeMismatchException() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(new MethodArgumentTypeMismatchException(null, String.class, "name", null, null));

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("invalidParameter"))
      .andExpect(jsonPath("$.errors[0].message").value("'name' must be of type String"));
  }

  @Test
  @DisplayName("MissingServletRequestParameterException")
  void testMissingServletRequestParameterException() throws Exception {
    // Setup
    MockMvc mockMvc = setupController(new MissingServletRequestParameterException("name", "String"));

    // Test and Verify
    mockMvc
      .perform(get("/failWithException"))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
      .andExpect(jsonPath("$.errors[0].message").value("Required request parameter 'name' for method parameter type String is not present"));
  }

  protected Object controllerAdvice() {
    return new SpringExceptionHandler();
  }
}
