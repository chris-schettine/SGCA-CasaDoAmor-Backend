package br.com.casadoamor.sgca.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import br.com.casadoamor.sgca.annotation.RateLimited;
import br.com.casadoamor.sgca.config.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;

class RateLimitAspectTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private RateLimitAspect aspect;

    @BeforeEach
    //@SuppressWarnings("unused")
    void setup() {
        MockitoAnnotations.openMocks(this);
        aspect = new RateLimitAspect(request);
    }

    @Test
    void whenBucketAllows_proceedsNormally() throws Throwable {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/test");

        // mock da annotation RateLimited
        RateLimited rl = mock(RateLimited.class);
        when(rl.limit()).thenReturn(10);
        when(rl.durationSeconds()).thenReturn(60);

        when(joinPoint.proceed()).thenReturn("ok");

        aspect.rateLimit(joinPoint, rl);

        verify(joinPoint).proceed();
    }

    @Test
    void whenBucketExhausted_throwsRateLimitExceeded() throws Throwable {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/test-exhaust");

        RateLimited rl = mock(RateLimited.class);
        when(rl.limit()).thenReturn(1);
        when(rl.durationSeconds()).thenReturn(1);

        // primeira chamada consome o bucket
        aspect.rateLimit(joinPoint, rl);

        // segunda chamada deve lançar exceção
        assertThatThrownBy(() -> aspect.rateLimit(joinPoint, rl))
                .isInstanceOf(RateLimitExceededException.class);
    }

}
