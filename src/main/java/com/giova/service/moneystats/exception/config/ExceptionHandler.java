package com.giova.service.moneystats.exception.config;

import com.giova.service.moneystats.exception.ValidationException;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import java.util.Arrays;
import java.util.Objects;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class ExceptionHandler extends UtilsException {

  /**
   * Handle Jakarta Validation Input
   *
   * @param ex ServerWebInputException to be handled
   * @param request ServerHttpRequest
   * @return ValidationException
   */
  @org.springframework.web.bind.annotation.ExceptionHandler(ServerWebInputException.class)
  public Mono<ResponseEntity<?>> handleServerWebInputException(
      ServerWebInputException ex, ServerHttpRequest request) {
    String message =
        ((WebExchangeBindException) ex)
            .getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList()
                .getFirst();
    ExceptionResponse response =
        ValidationException.getExceptionResponse(
            new ValidationException(message), request, ExceptionMap.ERR_VALID_MSS_001);
    response.getError().setMessage(message);
    response.getError().setExceptionMessage(null);
    return Mono.just(new ResponseEntity<>(response, ex.getStatusCode()));
  }

  /* OLD DATA */
  @org.springframework.web.bind.annotation.ExceptionHandler(
      value = DataIntegrityViolationException.class)
  public ResponseEntity<ExceptionResponse> handleException(
      DataIntegrityViolationException e, ServerHttpRequest request) {
    LOG.error(
        "An error happened while calling {} Downstream API: {}", request.getPath(), e.getMessage());
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
      MaxUploadSizeExceededException e, ServerHttpRequest request) {
    LOG.error(
        "An error happened while calling {} Downstream API: {}", request.getPath(), e.getMessage());
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ExceptionResponse response = getExceptionResponse(e, request, ExceptionMap.ERR_IMG_MSS_002);
    response.getError().setMessage(ExceptionMap.ERR_IMG_MSS_002.getMessage());
    if (e.getStackTrace().length != 0) {
      response.getError().setStackTrace(Arrays.toString(e.getStackTrace()));
    }
    return new ResponseEntity<>(response, status);
  }
}
