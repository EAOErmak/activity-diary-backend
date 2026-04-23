package com.example.activity_diary;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryLiquibaseChangelogTest {

    private static final Path OFFLINE_CHANGELOG_CSV = Path.of("databasechangelog.csv");

    @Test
    void masterChangelog_includesDropDictionaryItemChartTypeMigration() throws Exception {
        String master = readClasspathFile("db/changelog/db.changelog-master.xml");
        String migration = readClasspathFile("db/changelog/changes/drop/v1_drop_dictionary_item_chart_type.xml");

        assertThat(master)
                .contains("db/changelog/changes/drop/v1_drop_dictionary_item_chart_type.xml");
        assertThat(migration)
                .contains("tableName=\"dictionary_item\"")
                .contains("columnName=\"chart_type\"")
                .contains("<dropColumn");
    }

    @Test
    void liquibase_validatesMasterChangelogForPostgres() throws LiquibaseException {
        ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        Database database = DatabaseFactory.getInstance().openDatabase(
                "offline:postgresql?version=16",
                null,
                null,
                null,
                resourceAccessor
        );

        try {
            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    resourceAccessor,
                    database
            );
            liquibase.validate();
        } finally {
            database.close();
            deleteOfflineChangelogCsv();
        }
    }

    private static String readClasspathFile(String path) throws Exception {
        try (var stream = DictionaryLiquibaseChangelogTest.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing classpath resource: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void deleteOfflineChangelogCsv() {
        try {
            Files.deleteIfExists(OFFLINE_CHANGELOG_CSV);
        } catch (Exception ignored) {
        }
    }
}
