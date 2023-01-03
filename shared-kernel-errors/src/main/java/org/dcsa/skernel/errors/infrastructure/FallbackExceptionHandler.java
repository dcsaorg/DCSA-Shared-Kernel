package org.dcsa.skernel.errors.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.errors.transferobjects.ConcreteRequestErrorMessageTO;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Fallback exception handler for exceptions not handled elsewhere.
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class FallbackExceptionHandler extends BaseExceptionHandler {
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(HttpServletRequest httpServletRequest, Exception e) {
    log.warn("Unhandled exception {}: {}", e.getClass().getName(), e.getMessage(), e);
    return response(
      httpServletRequest,
      HttpStatus.INTERNAL_SERVER_ERROR,
      new ConcreteRequestErrorMessageTO("internalError", "Internal error")
    );
  }
}
