package org.dcsa.skernel.errors.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.errors.transferobjects.ConcreteRequestErrorMessageTO;
import org.dcsa.skernel.errors.transferobjects.RequestFailureTO;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Order(1)
@RestControllerAdvice
public class JakartaValidationExceptionHandler extends BaseExceptionHandler {
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<RequestFailureTO> handleConstraintViolationException(
    HttpServletRequest httpServletRequest, ConstraintViolationException e) {

    String exceptionMessage = null;
    if (e.getConstraintViolations() != null) {
      exceptionMessage =
        e.getConstraintViolations().stream()
          .filter(Objects::nonNull)
          .map(constraintViolation -> constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage())
          .collect(Collectors.joining(";"));
    }
    log.debug("Exception {}: {}; {}", e.getClass().getName(), e.getMessage(), exceptionMessage);

    return response(
      httpServletRequest,
      HttpStatus.BAD_REQUEST,
      new ConcreteRequestErrorMessageTO("invalidInput", exceptionMessage)
    );
  }
}
