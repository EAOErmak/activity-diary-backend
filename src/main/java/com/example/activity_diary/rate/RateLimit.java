package com.example.activity_diary.rate;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RateLimit {

    long capacity() default 10;   // Макс. кол-во запросов
    long refillTokens() default 10; // Сколько токенов добавлять
    long refillPeriodSeconds() default 60; // Интервал пополнения
    boolean usernameBased() default false;
}
