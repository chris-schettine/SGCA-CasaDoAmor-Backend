package br.com.casadoamor.sgca.config.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends RuntimeException {
  public RateLimitExceededException(String message) {
    super(message);
  }

  public HttpStatus getHttpStatus() {
    return HttpStatus.TOO_MANY_REQUESTS;
  }
}
