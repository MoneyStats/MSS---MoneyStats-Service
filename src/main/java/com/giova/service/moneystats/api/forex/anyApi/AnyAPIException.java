package com.giova.service.moneystats.api.forex.anyApi;

import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class AnyAPIException extends UtilsException {

  private static final ExceptionCode DEFAULT_CODE = ExceptionMap.ERR_ANY_API_001;

  public AnyAPIException(String message) {
    super(DEFAULT_CODE, message);
  }

  public AnyAPIException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode, message);
  }
}
