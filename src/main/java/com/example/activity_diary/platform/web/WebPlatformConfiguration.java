package com.example.activity_diary.platform.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("web")
@ComponentScan(basePackageClasses = WebPlatformConfiguration.class)
public class WebPlatformConfiguration {
}
