package br.com.casadoamor.sgca.config.exception;

import org.springframework.http.HttpStatus;

public class CustomError extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private final HttpStatus httpStatus;

    public CustomError(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
