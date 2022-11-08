package org.dcsa.skernel.errors.infrastructure;

import org.dcsa.skernel.errors.transferobjects.ConcreteRequestErrorMessageTO;
import org.dcsa.skernel.errors.transferobjects.RequestFailureTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

public abstract class BaseExceptionHandler {
  protected ResponseEntity<RequestFailureTO> response(HttpServletRequest request, HttpStatus httpStatus, ConcreteRequestErrorMessageTO... errors) {
    return response(request, httpStatus, Arrays.asList(errors));
  }

  protected ResponseEntity<RequestFailureTO> response(HttpServletRequest request, HttpStatus httpStatus, MultiValueMap<String, String> headers, ConcreteRequestErrorMessageTO... errors) {
    return response(request, httpStatus, headers, Arrays.asList(errors));
  }

  protected ResponseEntity<RequestFailureTO> response(HttpServletRequest request, HttpStatus httpStatus, List<ConcreteRequestErrorMessageTO> errors) {
    return response(request, httpStatus, null, errors);
  }

  protected ResponseEntity<RequestFailureTO> response(HttpServletRequest request, HttpStatus httpStatus, MultiValueMap<String, String> headers, List<ConcreteRequestErrorMessageTO> errors) {
    RequestFailureTO failureTO = new RequestFailureTO(request.getMethod(), request.getRequestURI(), errors, httpStatus);
    return new ResponseEntity<>(failureTO, headers, httpStatus);
  }
}
