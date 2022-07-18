package org.dcsa.skernel.errors.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;

public abstract class BaseExceptionHandlerTest {
  protected static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  protected MockMvc setupController(Exception exceptionToThrow) {
    return MockMvcBuilders.standaloneSetup(new ErrorController(exceptionToThrow))
      .setValidator(mock(org.springframework.validation.Validator.class))
      .setControllerAdvice(controllerAdvice())
      .build();
  }

  @RestController
  @AllArgsConstructor
  protected static class ErrorController {
    private final Exception exceptionToThrow;

    @GetMapping("/failWithException")
    public String failWithException() throws Exception {
      throw exceptionToThrow;
    }
  }

  protected abstract Object controllerAdvice();
}
