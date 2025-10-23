package br.com.casadoamor.sgca.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class RequestLoggingConfig {

    // Log básico de request (headers + query + payload)
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        filter.setAfterMessagePrefix("REQ DATA : ");
        return filter;
    }

    // Log também da response (payload enviado pelo backend)
    @Bean
    public OncePerRequestFilter responseLoggingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
                    throws ServletException, IOException {

                ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
                ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

                try {
                    filterChain.doFilter(requestWrapper, responseWrapper);
                } finally {
                    // Request payload
                    String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                    if (!requestBody.isBlank()) {
                        System.out.println("REQ " + request.getMethod() + " " + request.getRequestURI() +
                                " Body: " + maskSensitive(requestBody));
                    }

                    // Response payload
                    String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                    if (!responseBody.isBlank()) {
                        System.out.println("RES " + request.getMethod() + " " + request.getRequestURI() +
                                " Body: " + maskSensitive(responseBody));
                    }

                    // Importante: copiar a resposta de volta pro cliente
                    responseWrapper.copyBodyToResponse();
                }
            }
        };
    }

    // Pequeno helper para mascarar campos sensíveis
    private String maskSensitive(String body) {
        return body
                .replaceAll("(?i)\"senha\"\\s*:\\s*\".?\"", "\"senha\":\"**\"")
                .replaceAll("(?i)\"password\"\\s*:\\s*\".?\"", "\"password\":\"**\"")
                .replaceAll("(?i)\"token\"\\s*:\\s*\".?\"", "\"token\":\"**\"");
    }
}
