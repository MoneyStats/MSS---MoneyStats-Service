package com.giova.service.moneystats.app.attachments;

import com.giova.service.moneystats.exception.config.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class ImageException extends UtilsException {

  private static final ExceptionCode DEFAULT_CODE = ExceptionMap.ERR_IMG_MSS_001;

  public ImageException(String message) {
    super(DEFAULT_CODE, message);
  }

  public ImageException(String message, String exceptionMessage) {
    super(DEFAULT_CODE, message, exceptionMessage);
  }

  public ImageException(ExceptionCode exceptionCode) {
    super(exceptionCode, exceptionCode.getMessage());
  }

  public ImageException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode, message);
  }
}
