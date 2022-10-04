package org.dcsa.skernel.errors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends ConcreteRequestErrorMessageException {

  BadRequestException(String reason, Object reference, String message, Throwable cause) {
    super(reason, reference, message, cause);
  }
}
