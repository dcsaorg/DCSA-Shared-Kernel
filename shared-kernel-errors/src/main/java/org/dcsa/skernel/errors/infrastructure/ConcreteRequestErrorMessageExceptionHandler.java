package org.dcsa.skernel.errors.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.errors.transferobjects.RequestFailureTO;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Exception handler for internal exceptions.
 */
@Slf4j
@Order(1)
@RestControllerAdvice
public class ConcreteRequestErrorMessageExceptionHandler extends BaseExceptionHandler {
  @ExceptionHandler(ConcreteRequestErrorMessageException.class)
  public ResponseEntity<RequestFailureTO> handleConcreteRequestErrorMessageException(
    HttpServletRequest httpServletRequest, ConcreteRequestErrorMessageException e) {

    ResponseStatus responseStatusAnnotation = e.getClass().getAnnotation(ResponseStatus.class);
    HttpStatus httpStatus =
      responseStatusAnnotation != null ? responseStatusAnnotation.value() : HttpStatus.INTERNAL_SERVER_ERROR;

    if (httpStatus.is5xxServerError()) {
      log.warn("Exception {}: {}", e.getClass().getName(), e.getMessage(), e);
    } else {
      log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());
    }

    return response(httpServletRequest, httpStatus, e.asConcreteRequestMessage());
  }
}
