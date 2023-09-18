package com.giova.service.moneystats.api.coingecko;

import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class CoinGeckoException extends UtilsException {

  private static final ExceptionCode DEFAULT_CODE = ExceptionMap.ERR_COIN_GECKO_001;

  public CoinGeckoException(String message) {
    super(DEFAULT_CODE, message);
  }

  public CoinGeckoException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode, message);
  }
}
