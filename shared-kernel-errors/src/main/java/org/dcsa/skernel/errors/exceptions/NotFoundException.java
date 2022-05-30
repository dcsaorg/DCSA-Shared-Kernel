package org.dcsa.skernel.errors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends ConcreteRequestErrorMessageException {

  /**
   * @deprecated use {@link ConcreteRequestErrorMessageException#notFound(String)}.
   */
  @Deprecated
  public NotFoundException(String errorMessage) {
    super(null, null, errorMessage, null);
  }

  NotFoundException(String reason, Object reference, String message, Throwable cause) {
    super(reason, reference, message, cause);
  }
}
