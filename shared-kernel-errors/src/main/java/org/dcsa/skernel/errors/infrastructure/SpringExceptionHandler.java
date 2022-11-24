package org.dcsa.skernel.errors.infrastructure;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.errors.transferobjects.ConcreteRequestErrorMessageTO;
import org.dcsa.skernel.errors.transferobjects.RequestFailureTO;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Handler for Spring exceptions.
 */
@Slf4j
@Order(1)
@RestControllerAdvice
public class SpringExceptionHandler extends BaseExceptionHandler {
  private JavaxValidationExceptionHandler javaxValidationExceptionHandler = new JavaxValidationExceptionHandler();

  @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
  public ResponseEntity<RequestFailureTO> handleInvalidDataAccessResourceUsageException(
    HttpServletRequest httpServletRequest, InvalidDataAccessResourceUsageException e) {
    log.warn("Unhandled exception {}: {}", e.getClass().getName(), e.getMessage(), e);
    return response(
      httpServletRequest,
      HttpStatus.INTERNAL_SERVER_ERROR,
      new ConcreteRequestErrorMessageTO(
        "internalError",
        "Internal error with database operation"
      )
    );
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<RequestFailureTO> handleDataIntegrityViolationException(
    HttpServletRequest httpServletRequest, DataIntegrityViolationException e) {
    log.warn("Unhandled exception {}: {}", e.getClass().getName(), e.getMessage(), e);
    // For when the database catches an inconsistency.  They are not the best of error messages,
    // but we should ensure they at least have the proper HTTP code.
    return response(
      httpServletRequest,
      HttpStatus.CONFLICT,
      new ConcreteRequestErrorMessageTO(
        "conflict",
        "Internal error with database operation"
      )
    );
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<RequestFailureTO> handleHttpMessageNotReadableException(
    HttpServletRequest httpServletRequest, HttpMessageNotReadableException e) {
    log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());
    return response(
      httpServletRequest,
      HttpStatus.BAD_REQUEST,
      new ConcreteRequestErrorMessageTO(
        "invalidInput",
        e.getMessage()
      )
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<RequestFailureTO> handleMethodArgumentNotValidException(
    HttpServletRequest httpServletRequest, MethodArgumentNotValidException e) {
    log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());

    List<ConcreteRequestErrorMessageTO> errors =
      e.getBindingResult().getAllErrors().stream()
        .map(this::mapObjectError)
        .toList();

    return response(
      httpServletRequest,
      HttpStatus.BAD_REQUEST,
      errors
    );
  }

  private ConcreteRequestErrorMessageTO mapObjectError(ObjectError objectError) {
    if (objectError instanceof FieldError fieldError) {
      return new ConcreteRequestErrorMessageTO(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return new ConcreteRequestErrorMessageTO(objectError.getObjectName(), objectError.getDefaultMessage());
  }

  @ExceptionHandler(ServerWebInputException.class)
  public ResponseEntity<RequestFailureTO> handleServerWebInputException(
    HttpServletRequest httpServletRequest, ServerWebInputException e) {
    if (e.getMessage() != null && e.getMessage().contains("Invalid UUID string:")) {
      log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());
      return response(
        httpServletRequest,
        HttpStatus.BAD_REQUEST,
        new ConcreteRequestErrorMessageTO(
          "invalidParameter",
          "Input was not a valid UUID format"
        )
      );
    } else if (e.getCause() instanceof DecodingException) {
      return handleDecodingException(httpServletRequest, (DecodingException) e.getCause());
    } else if (e.getCause() instanceof ConstraintViolationException) {
      return javaxValidationExceptionHandler.handleConstraintViolationException(httpServletRequest, (ConstraintViolationException) e.getCause());
    } else {
      log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());
      return response(
        httpServletRequest,
        HttpStatus.BAD_REQUEST,
        new ConcreteRequestErrorMessageTO(
          "invalidInput",
          e.getMessage()
        )
      );
    }
  }

  @ExceptionHandler(DecodingException.class)
  public ResponseEntity<RequestFailureTO> handleDecodingException(
    HttpServletRequest httpServletRequest, DecodingException e) {

    log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());

    String attributeReference = "";
    if (e.getCause() instanceof JsonMappingException) {
      attributeReference = ((JsonMappingException) e.getCause()).getPathReference();
      // Remove leading java package name (Timestamp["foo"] looks better than
      // org.dcsa.jit.model.Timestamp["foo"])
      attributeReference = attributeReference.replaceFirst("^([a-zA-Z0-9]+[.])*+", "");
      attributeReference =
        " The error is associated with the attribute " + attributeReference + ".";
    }
    if (e.getCause() instanceof UnrecognizedPropertyException) {
      // Unwrap one layer of exception message (the outer message is "useless")
      attributeReference =
        e.getCause().getLocalizedMessage().replaceAll("\\s*\\([^)]*\\)\\s*", " ")
          + attributeReference;
    }

    if (e.getCause() instanceof InvalidFormatException ife) {
      // Per type messages where it makes sense to provide custom messages
      if (OffsetDateTime.class.isAssignableFrom(ife.getTargetType())) {
        attributeReference =
          "Invalid format for date time field. The value \""
            + ife.getValue()
            + "\" cannot be parsed via the patterns \"YYYY-MM-DD'T'HH:MM:SS+ZZZZ\" "
            + "or \"YYYY-MM-DD'T'HH:MM:SS'Z'\". Please check the input."
            + attributeReference;
      }
      if (LocalDate.class.isAssignableFrom(ife.getTargetType())) {
        attributeReference =
          "Invalid format for date field. The value \""
            + ife.getValue()
            + "\" cannot be parsed via the pattern \"YYYY-MM-DD\". Please check the input."
            + attributeReference;
      }
    }
    return response(
      httpServletRequest,
      HttpStatus.BAD_REQUEST,
      new ConcreteRequestErrorMessageTO("invalidInput", attributeReference)
    );
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<RequestFailureTO> handleMethodArgumentTypeMismatchException(
    HttpServletRequest httpServletRequest, MethodArgumentTypeMismatchException e) {
    log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());
    String propertyName = "'" + Objects.requireNonNullElse(e.getPropertyName(), e.getName()) + "'";
    String message =
      e.getRequiredType() != null
        ? propertyName + " must be of type " + e.getRequiredType().getSimpleName()
        : propertyName + " was the wrong type";
    return response(
      httpServletRequest,
      HttpStatus.BAD_REQUEST,
      new ConcreteRequestErrorMessageTO("invalidParameter", message)
    );
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<RequestFailureTO> handleMissingServletRequestParameterException(
    HttpServletRequest httpServletRequest, MissingServletRequestParameterException e) {
    log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());
    return response(
      httpServletRequest,
      HttpStatus.BAD_REQUEST,
      new ConcreteRequestErrorMessageTO("invalidInput", e.getMessage())
    );
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<RequestFailureTO> handleHttpMediaTypeNotSupportedException(
    HttpServletRequest httpServletRequest, HttpMediaTypeNotSupportedException e) {
    log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());
    return response(
      httpServletRequest,
      HttpStatus.UNSUPPORTED_MEDIA_TYPE,
      new ConcreteRequestErrorMessageTO("unsupported media type", e.getMessage())
    );
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<RequestFailureTO> handleHttpRequestMethodNotSupportedException(
    HttpServletRequest httpServletRequest, HttpRequestMethodNotSupportedException e) {
    log.debug("Exception {}: {}", e.getClass().getName(), e.getMessage());
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
      new ConcreteRequestErrorMessageTO("method not allowed", e.getMessage())
    );
  }
}
