package br.com.casadoamor.sgca.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int limit() default 10;         
    int durationSeconds() default 60; 
}
