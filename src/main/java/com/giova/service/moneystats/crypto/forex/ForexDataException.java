package com.giova.service.moneystats.crypto.forex;

import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class ForexDataException extends UtilsException {

  private static final ExceptionCode DEFAULT_CODE = ExceptionMap.ERR_FOR_DATA_001;

  public ForexDataException(String message) {
    super(DEFAULT_CODE, message);
  }

  public ForexDataException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode, message);
  }
}
