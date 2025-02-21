package com.giova.service.moneystats.crypto.asset;

import com.giova.service.moneystats.exception.config.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class AssetException extends UtilsException {
  private static final ExceptionCode DEFAULT_CODE = ExceptionMap.ERR_ASSET_001;

  public AssetException(String message) {
    super(DEFAULT_CODE, message);
  }
}
