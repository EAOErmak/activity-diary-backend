package com.example.activity_diary.platform.desktop;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("desktop")
@ComponentScan(basePackageClasses = DesktopPlatformConfiguration.class)
public class DesktopPlatformConfiguration {
}
