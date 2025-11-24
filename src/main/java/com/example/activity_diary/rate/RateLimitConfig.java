package com.example.activity_diary.rate;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitService rateLimitService) {
        return new RateLimitFilter(rateLimitService);
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            RateLimitFilter filter
    ) {
        FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);

        // Ограничиваем фильтр API-роутом
        bean.addUrlPatterns("/api/*");

        // Порядок относительно других фильтров
        bean.setOrder(2);

        return bean;
    }
}
