package com.giova.service.moneystats.authentication;

import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import org.springframework.http.HttpStatus;

public enum AuthException implements ExceptionCode {

    ERR_AUTH_MSS_001("AUTHENTICATION_EXCEPTION", HttpStatus.BAD_REQUEST, "Missing Value for: ");

    private final HttpStatus status;
    private final String message;
    private final String exceptionName;

    AuthException(String exceptionName, HttpStatus status, String message) {
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
