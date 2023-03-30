package com.giova.service.moneystats.app.attachments;

import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import org.springframework.http.HttpStatus;

public enum ImageException implements ExceptionCode {
  ERR_IMG_MSS_001("FILE_NOT_FOUND", HttpStatus.BAD_REQUEST, "File not found!"),
  ERR_IMG_MSS_002(
      "MaxUploadSizeExceeded",
      HttpStatus.BAD_REQUEST,
      "Maximum upload size exceeded! the request was rejected because its size exceeds the configured maximum (512000)");

  private final HttpStatus status;
  private final String message;
  private final String exceptionName;

  ImageException(String exceptionName, HttpStatus status, String message) {
    this.exceptionName = exceptionName;
    this.status = status;
    this.message = message;
  }

  @Override
  public String exceptionName() {
    return this.exceptionName;
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public HttpStatus getStatus() {
    return this.status;
  }
}
