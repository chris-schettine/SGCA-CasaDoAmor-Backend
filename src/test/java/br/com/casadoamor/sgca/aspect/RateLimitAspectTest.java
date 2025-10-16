package br.com.casadoamor.sgca.aspect;

import br.com.casadoamor.sgca.annotation.RateLimited;
import br.com.casadoamor.sgca.config.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RateLimitAspectTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private RateLimitAspect aspect;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        aspect = new RateLimitAspect(request);
    }

    @Test
    void whenBucketAllows_proceedsNormally() throws Throwable {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/test");

        RateLimited rl = new RateLimited() {
            public Class<? extends java.lang.annotation.Annotation> annotationType() { return RateLimited.class; }
            public int limit() { return 10; }
            public int durationSeconds() { return 60; }
        };

        when(joinPoint.proceed()).thenReturn("ok");

        Object res = aspect.rateLimit(joinPoint, rl);

        // proceed returned
        verify(joinPoint).proceed();
    }

    @Test
    void whenBucketExhausted_throwsRateLimitExceeded() throws Throwable {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/test-exhaust");

        RateLimited rl = new RateLimited() {
            public Class<? extends java.lang.annotation.Annotation> annotationType() { return RateLimited.class; }
            public int limit() { return 1; }
            public int durationSeconds() { return 1; }
        };

        // consume once
        aspect.rateLimit(joinPoint, rl);

        // second call should throw
        assertThatThrownBy(() -> aspect.rateLimit(joinPoint, rl))
                .isInstanceOf(RateLimitExceededException.class);
    }
}
