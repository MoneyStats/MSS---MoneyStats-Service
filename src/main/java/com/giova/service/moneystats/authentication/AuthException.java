package com.giova.service.moneystats.authentication;

import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class AuthException extends UtilsException {

  public AuthException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode, message);
  }
}
