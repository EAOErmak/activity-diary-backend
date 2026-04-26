package com.example.activity_diary.platform.desktop;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Profile("desktop")
public class DesktopDatabasePathInitializer implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String configuredPath = environment.getProperty("app.desktop.db.path");
        if (configuredPath == null || configuredPath.isBlank()) {
            return;
        }

        Path databasePath = Path.of(configuredPath).toAbsolutePath().normalize();
        Path parent = databasePath.getParent();
        if (parent == null) {
            return;
        }

        try {
            Files.createDirectories(parent);
        } catch (IOException ex) {
            throw new BeanInitializationException(
                    "Failed to create desktop database directory: " + parent,
                    ex
            );
        }
    }
}
