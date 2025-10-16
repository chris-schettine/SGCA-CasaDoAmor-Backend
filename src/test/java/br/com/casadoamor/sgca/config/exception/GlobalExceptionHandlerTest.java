package br.com.casadoamor.sgca.config.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import br.com.casadoamor.sgca.config.exception.RateLimitExceededException;
import br.com.casadoamor.sgca.config.exception.CustomError;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleCustomError_ReturnsStatus() {
        CustomError err = new CustomError("msg", HttpStatus.I_AM_A_TEAPOT);

        ResponseEntity<Map<String, Object>> res = handler.handleCustomError(err);

        assertThat(res.getStatusCode().value()).isEqualTo(418);
        assertThat(res.getBody()).containsKey("error");
    }

    @Test
    void handleGenericException_Returns500() {
        Exception ex = new Exception("boom");
        ResponseEntity<Map<String, Object>> res = handler.handleGenericException(ex);

        assertThat(res.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void handleRateLimitExceeded_Returns429() {
        RateLimitExceededException ex = new RateLimitExceededException("too many");
        ResponseEntity<Map<String, Object>> res = handler.handleRateLimitExceeded(ex);

        assertThat(res.getStatusCode().value()).isEqualTo(429);
    }
}
