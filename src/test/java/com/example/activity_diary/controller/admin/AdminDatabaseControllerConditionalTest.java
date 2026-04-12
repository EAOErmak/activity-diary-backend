package com.example.activity_diary.controller.admin;

import com.example.activity_diary.entity.enums.TableType;
import com.example.activity_diary.service.admin.AdminDatabaseService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class AdminDatabaseControllerConditionalTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
            .withUserConfiguration(AdminDatabaseController.class, TestConfig.class);

    @Test
    void controllerIsEnabledByDefault() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(AdminDatabaseController.class)
        );
    }

    @Test
    void controllerIsEnabledWhenPropertyIsTrue() {
        contextRunner
                .withPropertyValues("app.admin.database.clear.enabled=true")
                .run(context ->
                        assertThat(context).hasSingleBean(AdminDatabaseController.class)
                );
    }

    @Test
    void controllerIsEnabledWhenPropertyIsFalse() {
        contextRunner
                .withPropertyValues("app.admin.database.clear.enabled=false")
                .run(context ->
                        assertThat(context).hasSingleBean(AdminDatabaseController.class)
                );
    }

    @Configuration
    static class TestConfig {

        @Bean
        AdminDatabaseService adminDatabaseService() {
            return new AdminDatabaseService() {
                @Override
                public int clearAllTables() {
                    return 0;
                }

                @Override
                public void clearTable(TableType tableType) {
                }
            };
        }
    }
}
