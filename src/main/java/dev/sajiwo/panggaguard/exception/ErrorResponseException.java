package dev.sajiwo.panggaguard.exception;

import org.springframework.http.HttpStatus;

public class ErrorResponseException extends org.springframework.web.ErrorResponseException {

  private Throwable cause;

  private String message;

  public ErrorResponseException(HttpStatus status, String message) {
    this(status, message, null);
  }

  public ErrorResponseException(HttpStatus status, String message, Throwable cause) {
    super(status);
    this.message = message;
    this.cause = cause;
  }

  public String getMessage() {
    return message;
  }

  public Throwable getCause() {
    return cause;
  }

  public static ErrorResponseException notFound(String message) {
    return new ErrorResponseException(HttpStatus.NOT_FOUND, message);
  }

  public static ErrorResponseException internalServerError() {
    return new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
  }

  public static ErrorResponseException internalServerError(Throwable cause) {
    return new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", cause);
  }
}
