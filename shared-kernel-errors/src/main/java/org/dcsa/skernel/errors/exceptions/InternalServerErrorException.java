package org.dcsa.skernel.errors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends ConcreteRequestErrorMessageException {

  InternalServerErrorException(String reason, Object reference, String message, Throwable cause) {
    super(reason, reference, message, cause);
  }
}
