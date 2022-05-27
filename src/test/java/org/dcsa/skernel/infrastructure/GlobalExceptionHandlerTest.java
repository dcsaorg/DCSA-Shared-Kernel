package org.dcsa.skernel.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.dcsa.skernel.exceptions.ConcreteRequestErrorMessageException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@DisplayName("Tests for GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

  static MockMvc mockMvc;

  static ObjectMapper objectMapper;

  @BeforeAll
  public static void setup() {
    mockMvc =  MockMvcBuilders.standaloneSetup(new TestController())
      .setValidator(mock(org.springframework.validation.Validator.class))
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

  }
  @RestController
  static class TestController {

    @SneakyThrows
    @GetMapping("/test")
    public String test(@RequestParam("kind") String kind) {
      throw switch (kind) {
        case "notFound" -> ConcreteRequestErrorMessageException.notFound("You can't find me.");
        case "invalidInput" -> ConcreteRequestErrorMessageException.invalidInput(
          "Invalid input", new IllegalArgumentException());
        case "internalError" -> ConcreteRequestErrorMessageException.internalServerError("Internal server error");
        case "validateObjectManually" -> validateObject();
        case "validateObjectServerInput" -> new ServerWebInputException("", null, validateObject());
        case "invalidDataAccessResourceException" -> new InvalidDataAccessResourceUsageException("test message");
        case "invalidUUIDServerInput" -> new ServerWebInputException("Invalid UUID string:");
        case "emptyDecodingException" -> new ServerWebInputException("DecodingException", null, new DecodingException(""));
        case "genericServerWebInputException" -> new ServerWebInputException("");
        case "jsonDecodeUnrecognizedProperty" -> parseInvalidJson("{\"TestPoJo\": {\"test\":\"value\"}}");
        case "jsonDecodeInvalidDate" -> parseInvalidJson("{\"testString\":\"value\", \"testDate\": \"01-12-2022\"}");
        case "jsonDecodeInvalidDateTime" -> parseInvalidJson("{\"testString\":\"value\", \"testDateTime\": \"2022-01-23T35:12:00\"}");
        case "dataIntegrityViolation" -> new DataIntegrityViolationException("Data needs to have integrity");
        case "genericException" -> new Exception("Generic exception");
        case "responseStatusExceptionInternalError" -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        default -> new AssertionError("Unknown kind");
      };
    }

    private Throwable parseInvalidJson(String json) {
      try {
        objectMapper.readValue(json, TestPoJo.class);
        throw new AssertionError("readValue should have thrown an exception");
      } catch (JsonProcessingException e) {
        return new DecodingException("JsonPropertyException", e);
      }
    }

    private Throwable validateObject() {
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
  }

  @DisplayName("Tests for ConcreteRequestErrorMessageException")
  @Nested
  class ConcreteRequestErrorMessageExceptionTest {

    @Test
    @DisplayName("Test for not found exception")
    void testNotFound() throws Exception {
      mockMvc
        .perform(get("/test?kind=notFound"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("notFound"))
        .andExpect(jsonPath("$.errors[0].message").value("You can't find me."));
    }

    @Test
    @DisplayName("Test for invalid input exception")
    void testInvalidInput() throws Exception {
      mockMvc
        .perform(get("/test?kind=invalidInput"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
        .andExpect(jsonPath("$.errors[0].message").value("Invalid input"));
    }

    @Test
    @DisplayName("Test for internal server error")
    void testInternalServerError() throws Exception {
      mockMvc
        .perform(get("/test?kind=internalError"))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("internalError"))
        .andExpect(jsonPath("$.errors[0].message").value("Internal server error"));

    }
  }

  @DisplayName("Tests for constraint violation exceptions")
  @Nested
  class ConstraintViolationExceptionTest {

    @Test
    @DisplayName("Test constraint violation with invalid input")
    void testConstraintViolationExceptionInputParameter() throws Exception {
      mockMvc
        .perform(get("/test?kind=validateObjectManually"))
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
      mockMvc
        .perform(get("/test?kind=validateObjectServerInput"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
        .andExpect(jsonPath("$.errors[0].message").value(anyOf(
          equalTo("limitedField size must be between 0 and 5;requiredField must not be null"),
          equalTo("requiredField must not be null;limitedField size must be between 0 and 5")
        )));
    }
  }

  @Test
  @DisplayName("Test InvalidDataAccessResourceUsageException")
  void testSqlStateException() throws Exception {
    mockMvc
      .perform(get("/test?kind=invalidDataAccessResourceException"))
      .andDo(print())
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.httpMethod").value("GET"))
      .andExpect(jsonPath("$.errors[0].reason").value("internalError"))
      .andExpect(jsonPath("$.errors[0].message").value("Internal error with database operation - please see log"));
  }

  @DisplayName("Tests for ServerWebInputException")
  @Nested
  class ServerWebInputExceptionTest {

    @Test
    @DisplayName("Test for ServerWebInputException invalid UUID")
    void testServerWebInputInvalidUUIDException() throws Exception {
      mockMvc
        .perform(get("/test?kind=invalidUUIDServerInput"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidParameter"))
        .andExpect(jsonPath("$.errors[0].message").value("Input was not a valid UUID format"));
    }

    @Test
    @DisplayName("Test for generic ServerWebInputException")
    void testServerWebInputException() throws Exception {
      mockMvc
        .perform(get("/test?kind=genericServerWebInputException"))
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
      mockMvc
        .perform(get("/test?kind=emptyDecodingException"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"));
    }


    @Test
    @DisplayName("Test for JsonPropertyException as cause of UnrecognizedPropertyException")
    void testDecodingExceptionCausedByUnrecognizedPropertyException() throws Exception {
      mockMvc
        .perform(get("/test?kind=jsonDecodeUnrecognizedProperty"))
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
      mockMvc
        .perform(get("/test?kind=jsonDecodeInvalidDate"))
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
      mockMvc
        .perform(get("/test?kind=jsonDecodeInvalidDateTime"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
        .andExpect(jsonPath("$.errors[0].message").value(
          "Invalid format for date time field. The value \"2022-01-23T35:12:00\" " +
            "cannot be parsed via the patterns \"YYYY-MM-DD'T'HH:MM:SS+ZZZZ\" or \"YYYY-MM-DD'T'HH:MM:SS'Z'\". " +
            "Please check the input. The error is associated with the attribute TestPoJo[\"testDateTime\"]."));
    }
  }

  @DisplayName("Tests for DataIntegrityViolationException")
  @Nested
  class DataIntegrityViolationExceptionTest {

    @Test
    @DisplayName("DataIntegrityViolationException")
    void testDataIntegrityViolationException() throws Exception {
      mockMvc
        .perform(get("/test?kind=dataIntegrityViolation"))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("conflict"))
        .andExpect(jsonPath("$.errors[0].message").value("Data needs to have integrity"));
    }
  }

  @DisplayName("Tests for Generic Exception")
  @Nested
  class GenericExceptionTest {

    @Test
    @DisplayName("Test for a generic exception")
    void testGenericException() throws Exception {
      mockMvc
        .perform(get("/test?kind=genericException"))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("internalError"));
    }

    @Test
    @DisplayName("Test for a ResponseStatusException")
    void testResponseStatusException() throws Exception {
      mockMvc
        .perform(get("/test?kind=responseStatusExceptionInternalError"))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.httpMethod").value("GET"))
        .andExpect(jsonPath("$.errors[0].reason").value("internalError"))
        .andExpect(jsonPath("$.errors[0].message").value("500 INTERNAL_SERVER_ERROR"));
    }
  }
}
