package br.com.casadoamor.sgca.aspect;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.annotation.RateLimited;
import br.com.casadoamor.sgca.config.exception.RateLimitExceededException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final HttpServletRequest request;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String key = getClientKey();

        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(rateLimited));

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        }

        throw new RateLimitExceededException("Too many requests - try again later");
    }

    private String getClientKey() {
        return request.getRemoteAddr() + ":" + request.getRequestURI();
    }

    private Bucket createBucket(RateLimited rateLimited) {
        Bandwidth limit = Bandwidth.classic(
                rateLimited.limit(),
                Refill.greedy(rateLimited.limit(), Duration.ofSeconds(rateLimited.durationSeconds()))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    public static class RateLimitException extends RuntimeException {
        public final int limit;
        public final int duration;
        public RateLimitException(String message, int limit, int duration) {
            super(message);
            this.limit = limit;
            this.duration = duration;
        }
    }
}
