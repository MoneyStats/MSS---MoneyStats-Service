package com.giova.service.moneystats.exception;

import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import java.util.Arrays;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class ExceptionHandler extends UtilsException {

  @org.springframework.web.bind.annotation.ExceptionHandler(
      value = DataIntegrityViolationException.class)
  public ResponseEntity<ExceptionResponse> handleException(
      DataIntegrityViolationException e, HttpServletRequest request) {
    LOG.error(
        "An error happened while calling {} Downstream API: {}",
        request.getRequestURI(),
        e.getMessage());
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ExceptionResponse response = getExceptionResponse(e, request, ExceptionMap.ERR_AUTH_MSS_003);
    String value = "";
    if (e.getCause() instanceof ConstraintViolationException)
      if (((ConstraintViolationException) e.getCause())
          .getSQLException()
          .getMessage()
          .contains("Duplicate entry")) {
        value =
            Objects.requireNonNull(
                ((ConstraintViolationException) e.getCause())
                    .getSQLException()
                    .getMessage()
                    .split(" for key")[0]);
        response
            .getError()
            .setExceptionMessage(
                Objects.requireNonNull((ConstraintViolationException) e.getCause())
                    .getSQLException()
                    .getMessage());
      } else {
        value =
            "Missing Value for: "
                + e.getCause()
                    .getMessage()
                    .split("\\.")[e.getCause().getMessage().split("\\.").length - 1];
      }
    response.getError().setMessage(value);
    if (e.getStackTrace().length != 0) {
      response.getError().setStackTrace(Arrays.toString(e.getStackTrace()));
      LOG.error(Arrays.toString(e.getStackTrace()));
    }
    return new ResponseEntity<>(response, status);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(
      value = MaxUploadSizeExceededException.class)
  public ResponseEntity<ExceptionResponse> handleException(
      MaxUploadSizeExceededException e, HttpServletRequest request) {
    LOG.error(
        "An error happened while calling {} Downstream API: {}",
        request.getRequestURI(),
        e.getMessage());
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ExceptionResponse response = getExceptionResponse(e, request, ExceptionMap.ERR_IMG_MSS_002);
    response.getError().setMessage(ExceptionMap.ERR_IMG_MSS_002.getMessage());
    if (e.getStackTrace().length != 0) {
      response.getError().setStackTrace(Arrays.toString(e.getStackTrace()));
    }
    return new ResponseEntity<>(response, status);
  }
}
