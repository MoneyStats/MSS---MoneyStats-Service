package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class AuthException extends UtilsException {
  private static final ExceptionCode DEFAULT_CODE = ExceptionMap.ERR_AUTH_MSS_003;

  public AuthException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode, message);
  }
}
