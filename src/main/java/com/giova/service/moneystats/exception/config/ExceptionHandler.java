package com.giova.service.moneystats.exception.config;

import com.giova.service.moneystats.exception.ValidationException;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MissingRequestValueException;
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

  /**
   * Handle MissingRequestValueException
   *
   * @param ex ServerWebInputException to be handled
   * @param request ServerHttpRequest
   * @return ValidationException
   */
  @org.springframework.web.bind.annotation.ExceptionHandler(MissingRequestValueException.class)
  public Mono<ResponseEntity<?>> handleMissingRequestValueException(
      MissingRequestValueException ex, ServerHttpRequest request) {
    ExceptionResponse response =
        ValidationException.getExceptionResponse(
            new ValidationException(ex.getReason()), request, ExceptionMap.ERR_VALID_MSS_001);
    response.getError().setMessage(ex.getReason());
    response.getError().setExceptionMessage(null);
    return Mono.just(new ResponseEntity<>(response, ex.getStatusCode()));
  }

  /**
   * Handle DataBufferLimitException
   *
   * @param ex DataBufferLimitException to be handled
   * @param request ServerHttpRequest
   * @return ValidationException
   */
  @org.springframework.web.bind.annotation.ExceptionHandler(DataBufferLimitException.class)
  public Mono<ResponseEntity<?>> handleDataBufferLimitException(
      DataBufferLimitException ex, ServerHttpRequest request) {
    ExceptionResponse response =
        ValidationException.getExceptionResponse(
            new ValidationException(ex.getMessage()), request, ExceptionMap.ERR_VALID_MSS_001);
    response.getError().setMessage(ex.getMessage());
    response.getError().setExceptionMessage(null);
    return Mono.just(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));
  }
}
