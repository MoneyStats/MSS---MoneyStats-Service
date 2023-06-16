package com.giova.service.moneystats.exception;

import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import org.springframework.http.HttpStatus;

public enum ExceptionMap implements ExceptionCode {
  // Application
  ERR_APP_MSS_001("MATH_EXCEPTION", HttpStatus.BAD_REQUEST, "Error on rounding value, try again!"),
  // Authentication
  ERR_AUTH_MSS_001("AUTHENTICATION_EXCEPTION", HttpStatus.BAD_REQUEST, "Missing Value for: "),
  ERR_AUTH_MSS_002("TOKEN_PARSE", HttpStatus.UNAUTHORIZED, "Error during parsing Access-Token"),
  ERR_AUTH_MSS_003(
      "WRONG_CREDENTIAL",
      HttpStatus.BAD_REQUEST,
      "Wrong Credential for username or password. Try again!"),
  ERR_AUTH_MSS_004(
      "CHECK_LOGIN_FAIL",
      HttpStatus.UNAUTHORIZED,
      "Error on checking the current user, Login again!"),
  ERR_AUTH_MSS_005(
      "INVALID_REGISTER_TOKEN",
      HttpStatus.UNAUTHORIZED,
      "Error on checking the current token provided, wrong token, try again!"),
  ERR_AUTH_MSS_006("INVALID_EMAIL", HttpStatus.BAD_REQUEST, "Invalid email address, Try Again!"),
  // Image
  ERR_IMG_MSS_001("FILE_NOT_FOUND", HttpStatus.BAD_REQUEST, "File not found!"),
  ERR_IMG_MSS_002(
      "MaxUploadSizeExceeded",
      HttpStatus.BAD_REQUEST,
      "Maximum upload size exceeded! the request was rejected because its size exceeds the configured maximum (512000)"),
  // EmailSender
  ERR_EMAIL_SEND_001("CLIENT_EXCEPTION", HttpStatus.BAD_REQUEST, "Error on client: "),
  ERR_EMAIL_SEND_002(
      "STRING_EXCEPTION", HttpStatus.BAD_REQUEST, "Error on converting string for templates"),
  ERR_JSON_FOR_001("JSON_FORMAT_EXCEPTION", HttpStatus.BAD_REQUEST, "Error on converting object");

  private final HttpStatus status;
  private final String message;
  private final String exceptionName;

  ExceptionMap(String exceptionName, HttpStatus status, String message) {
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
