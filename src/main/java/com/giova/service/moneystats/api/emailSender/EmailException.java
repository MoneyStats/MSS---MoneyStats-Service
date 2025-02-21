package com.giova.service.moneystats.api.emailSender;

import com.giova.service.moneystats.exception.config.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class EmailException extends UtilsException {

  private static final ExceptionCode DEFAULT_CODE = ExceptionMap.ERR_EMAIL_SEND_001;

  public EmailException(String message) {
    super(DEFAULT_CODE, message);
  }

  public EmailException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode, message);
  }
}
