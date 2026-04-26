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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryLiquibaseChangelogTest {

    private static final Path OFFLINE_CHANGELOG_CSV = Path.of("databasechangelog.csv");
    private static final List<String> SQLITE_BASELINE_FILES = List.of(
            "db/changelog/sqlite/baseline/001-users.xml",
            "db/changelog/sqlite/baseline/002-dictionary-item.xml",
            "db/changelog/sqlite/baseline/003-tag.xml",
            "db/changelog/sqlite/baseline/004-user-accounts.xml",
            "db/changelog/sqlite/baseline/005-verification-codes.xml",
            "db/changelog/sqlite/baseline/006-refresh-token.xml",
            "db/changelog/sqlite/baseline/007-login-event.xml",
            "db/changelog/sqlite/baseline/008-registration-event.xml",
            "db/changelog/sqlite/baseline/009-general-food.xml",
            "db/changelog/sqlite/baseline/010-user-food.xml",
            "db/changelog/sqlite/baseline/011-diary-entry.xml",
            "db/changelog/sqlite/baseline/012-entry-metric.xml",
            "db/changelog/sqlite/baseline/013-entry-metric-value.xml",
            "db/changelog/sqlite/baseline/014-user-tag.xml",
            "db/changelog/sqlite/baseline/015-diary-entry-tag.xml",
            "db/changelog/sqlite/baseline/016-tag-usage-agg.xml",
            "db/changelog/sqlite/baseline/017-metric-usage-agg.xml",
            "db/changelog/sqlite/baseline/018-diary-entry-template.xml",
            "db/changelog/sqlite/baseline/019-entry-template-metric.xml",
            "db/changelog/sqlite/baseline/020-entry-template-metric-value.xml",
            "db/changelog/sqlite/baseline/021-day-template.xml",
            "db/changelog/sqlite/baseline/022-week-template.xml",
            "db/changelog/sqlite/baseline/023-template-day-item.xml",
            "db/changelog/sqlite/baseline/024-template-entry-item.xml",
            "db/changelog/sqlite/baseline/025-week-goal.xml",
            "db/changelog/sqlite/baseline/026-day-goal.xml",
            "db/changelog/sqlite/baseline/027-diary-entry-goal.xml",
            "db/changelog/sqlite/baseline/028-entry-metric-goal.xml",
            "db/changelog/sqlite/baseline/029-entry-metric-value-goal.xml",
            "db/changelog/sqlite/baseline/030-metric-name-unit-link.xml",
            "db/changelog/sqlite/baseline/031-tag-chart-type-link.xml",
            "db/changelog/sqlite/baseline/032-tag-metric-link.xml"
    );

    @Test
    void masterChangelog_routesPostgresAndSqliteSeparately() throws Exception {
        String master = readClasspathFile("db/changelog/db.changelog-master.xml");

        assertThat(master)
                .contains("db/changelog/postgres/db.postgres-master.xml\" contextFilter=\"web\"")
                .contains("db/changelog/sqlite/db.sqlite-baseline.xml\" contextFilter=\"desktop\"")
                .contains("db/changelog/shared/db.shared-master.xml");
    }

    @Test
    void postgresMaster_keepsDropDictionaryItemChartTypeMigration() throws Exception {
        String postgresMaster = readClasspathFile("db/changelog/postgres/db.postgres-master.xml");
        String migration = readClasspathFile("db/changelog/changes/drop/v1_drop_dictionary_item_chart_type.xml");

        assertThat(postgresMaster)
                .contains("db/changelog/changes/drop/v1_drop_dictionary_item_chart_type.xml");
        assertThat(migration)
                .contains("tableName=\"dictionary_item\"")
                .contains("columnName=\"chart_type\"")
                .contains("<dropColumn");
    }

    @Test
    void sqliteBaseline_entrypointIncludesSingleChangeSetFilesInOrder() throws Exception {
        String sqliteBaseline = readClasspathFile("db/changelog/sqlite/db.sqlite-baseline.xml");

        int lastIndex = -1;
        for (String baselineFile : SQLITE_BASELINE_FILES) {
            int currentIndex = sqliteBaseline.indexOf(baselineFile);
            assertThat(currentIndex).as(baselineFile).isGreaterThan(lastIndex);
            lastIndex = currentIndex;
        }

        assertThat(sqliteBaseline)
                .doesNotContain("001-users-auth.xml")
                .doesNotContain("002-dictionary-tags.xml")
                .doesNotContain("003-food.xml")
                .doesNotContain("004-diary-entries.xml")
                .doesNotContain("005-analytics-aggregates.xml")
                .doesNotContain("006-templates.xml")
                .doesNotContain("007-goals.xml")
                .doesNotContain("008-admin-links.xml");
    }

    @Test
    void sqliteBaseline_changesetsRemainExplicitlyGuardedAndSingleFile() throws Exception {
        for (String baselineFile : SQLITE_BASELINE_FILES) {
            String changelog = readClasspathFile(baselineFile);

            assertThat(changelog)
                    .contains("dbms=\"sqlite\"")
                    .contains("context=\"desktop\"");
            assertThat(countOccurrences(changelog, "<changeSet ")).as(baselineFile).isEqualTo(1);
        }

        assertThat(readClasspathFile("db/changelog/sqlite/baseline/001-users.xml"))
                .contains("CREATE TABLE users");
        assertThat(readClasspathFile("db/changelog/sqlite/baseline/013-entry-metric-value.xml"))
                .contains("CREATE TABLE entry_metric_value");
        assertThat(readClasspathFile("db/changelog/sqlite/baseline/032-tag-metric-link.xml"))
                .contains("CREATE TABLE tag_metric_link");
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

    private static int countOccurrences(String text, String token) {
        int count = 0;
        int fromIndex = 0;
        while ((fromIndex = text.indexOf(token, fromIndex)) >= 0) {
            count++;
            fromIndex += token.length();
        }
        return count;
    }
}
