package br.com.casadoamor.sgca.config.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import br.com.casadoamor.sgca.dto.common.ErroResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomError.class)
    public ResponseEntity<Object> handleCustomException(CustomError ex) {
        ErroResponseDTO errorResponse = new ErroResponseDTO(ex.getMessage());
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex) {
        String fieldName = ex.getPath().get(0).getFieldName();
        String invalidValue = ex.getValue().toString();       

        Map<String, String> error = new HashMap<>();
        error.put("message", "Invalid value for the field '" + fieldName + "': '" + invalidValue + "'.");
        error.put("details", "Please ensure the value matches one of the allowed enum values.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
