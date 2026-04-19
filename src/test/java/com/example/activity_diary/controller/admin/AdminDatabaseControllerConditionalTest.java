package com.example.activity_diary.controller.admin;

import com.example.activity_diary.entity.enums.TableType;
import com.example.activity_diary.platform.api.controller.admin.AdminDatabaseController;
import com.example.activity_diary.service.admin.AdminDatabaseService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AdminDatabaseControllerConditionalTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
            .withUserConfiguration(AdminDatabaseController.class)
            .withBean(AdminDatabaseService.class, () -> new AdminDatabaseService() {
                @Override
                public int clearAllTables() {
                    return 0;
                }

                @Override
                public void clearTable(TableType tableType) {
                }
            });

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
}
