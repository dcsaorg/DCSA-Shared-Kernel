package org.dcsa.skernel.errors.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.errors.transferobjects.ConcreteRequestErrorMessageTO;
import org.dcsa.skernel.errors.transferobjects.RequestFailureTO;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Order(1)
@RestControllerAdvice
public class MethodNotSupposedExceptionHandler extends BaseExceptionHandler {

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<RequestFailureTO> handleException(HttpServletRequest httpServletRequest, HttpRequestMethodNotSupportedException e) {
    MultiValueMap<String, String> headers = null;
    String[] methods = e.getSupportedMethods();
    if (methods != null) {
      headers = new HttpHeaders();
      headers.add("Allow", String.join(", ", methods));
    }
    return response(
      httpServletRequest,
      HttpStatus.METHOD_NOT_ALLOWED,
      headers,
      new ConcreteRequestErrorMessageTO("internalError", "Internal error")
    );
  }
}
