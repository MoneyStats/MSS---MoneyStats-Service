package com.giova.service.moneystats.api.forex.exchageRates;

import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class ExchangeRatesException extends UtilsException {

  private static final ExceptionCode DEFAULT_CODE = ExceptionMap.ERR_EXC_RATES_001;

  public ExchangeRatesException(String message) {
    super(DEFAULT_CODE, message);
  }

  public ExchangeRatesException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode, message);
  }
}
