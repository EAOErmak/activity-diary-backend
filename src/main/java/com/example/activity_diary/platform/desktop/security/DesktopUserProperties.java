package com.example.activity_diary.platform.desktop.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.desktop.user")
public class DesktopUserProperties {

    private String username = "desktop";
    private String fullName = "Desktop User";
    private String providerId = "desktop-local-user";
}
