package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.entity.enums.TableType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.platform.desktop.security.DesktopUserBootstrap;
import com.example.activity_diary.service.admin.AdminDatabaseService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDatabaseServiceImpl implements AdminDatabaseService {

    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectProvider<DesktopUserBootstrap> desktopUserBootstrapProvider;

    @Override
    @Transactional
    public int clearAllTables() {
        DatabaseRelationships relationships = loadRelationships();
        List<String> allTableNames = orderTablesForDeletion(getAllTableNames(), relationships.parentsByChild());
        log.info("Admin database full clear requested for {} tables", allTableNames.size());
        clearResolvedTables(allTableNames);
        reinitializeDesktopUserIfNeeded();
        return allTableNames.size();
    }

    @Override
    @Transactional
    public void clearTable(TableType tableType) {
        if (tableType == null) {
            throw new BadRequestException("Table type is required");
        }

        DatabaseRelationships relationships = loadRelationships();
        List<String> resolvedTableNames = resolveCascadeDeletionOrder(tableType.getTableName(), relationships);
        log.info("Admin database table clear requested: target={}, resolvedTables={}",
                tableType.getTableName(), resolvedTableNames);

        clearResolvedTables(resolvedTableNames);
        reinitializeDesktopUserIfNeeded();
    }

    private void clearResolvedTables(List<String> tableNames) {
        if (tableNames.isEmpty()) {
            return;
        }

        entityManager.flush();
        entityManager.clear();

        String databaseProductName = getDatabaseProductName();
        if (isPostgreSql(databaseProductName)) {
            truncateTables(tableNames);
        } else {
            deleteTables(tableNames);
            resetSqliteSequencesIfPresent(tableNames, databaseProductName);
        }

        entityManager.clear();
        log.info("Admin database clear finished: database={}, tables={}", databaseProductName, tableNames);
    }

    private void truncateTables(List<String> tableNames) {
        String joinedTables = tableNames.stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));

        jdbcTemplate.execute("TRUNCATE TABLE " + joinedTables + " RESTART IDENTITY CASCADE");
    }

    private void deleteTables(List<String> tableNames) {
        for (String tableName : tableNames) {
            jdbcTemplate.execute("DELETE FROM " + quoteIdentifier(tableName));
        }
    }

    private void resetSqliteSequencesIfPresent(List<String> tableNames, String databaseProductName) {
        if (!isSqlite(databaseProductName) || !tableExists("sqlite_sequence")) {
            return;
        }

        for (String tableName : tableNames) {
            jdbcTemplate.update("DELETE FROM sqlite_sequence WHERE name = ?", tableName);
        }
    }

    private List<String> resolveCascadeDeletionOrder(String rootTableName, DatabaseRelationships relationships) {
        LinkedHashSet<String> targetTables = new LinkedHashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(rootTableName);

        while (!stack.isEmpty()) {
            String tableName = stack.pop();
            if (!targetTables.add(tableName)) {
                continue;
            }

            for (String childTableName : relationships.childrenByParent().getOrDefault(tableName, Set.of())) {
                stack.push(childTableName);
            }
        }

        return orderTablesForDeletion(targetTables, relationships.parentsByChild());
    }

    private List<String> orderTablesForDeletion(
            Collection<String> targetTables,
            Map<String, Set<String>> parentsByChild
    ) {
        LinkedHashSet<String> tablesToOrder = new LinkedHashSet<>(targetTables);
        Map<String, Set<String>> outgoingEdges = new LinkedHashMap<>();
        Map<String, Integer> indegree = new LinkedHashMap<>();

        for (String tableName : tablesToOrder) {
            outgoingEdges.put(tableName, new LinkedHashSet<>());
            indegree.put(tableName, 0);
        }

        for (String childTableName : tablesToOrder) {
            for (String parentTableName : parentsByChild.getOrDefault(childTableName, Set.of())) {
                if (!tablesToOrder.contains(parentTableName)) {
                    continue;
                }

                if (outgoingEdges.get(childTableName).add(parentTableName)) {
                    indegree.put(parentTableName, indegree.get(parentTableName) + 1);
                }
            }
        }

        TreeSet<String> ready = new TreeSet<>(Comparator.naturalOrder());
        for (Map.Entry<String, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                ready.add(entry.getKey());
            }
        }

        List<String> orderedTableNames = new ArrayList<>(tablesToOrder.size());
        while (!ready.isEmpty()) {
            String tableName = ready.pollFirst();
            orderedTableNames.add(tableName);

            for (String nextTableName : outgoingEdges.getOrDefault(tableName, Set.of())) {
                int nextIndegree = indegree.get(nextTableName) - 1;
                indegree.put(nextTableName, nextIndegree);
                if (nextIndegree == 0) {
                    ready.add(nextTableName);
                }
            }
        }

        if (orderedTableNames.size() == tablesToOrder.size()) {
            return orderedTableNames;
        }

        log.warn("Falling back to enum order for table deletion because dependency ordering was incomplete: {}",
                tablesToOrder);

        return getAllTableNames().stream()
                .filter(tablesToOrder::contains)
                .toList();
    }

    private DatabaseRelationships loadRelationships() {
        List<String> appTableNames = getAllTableNames();
        Set<String> appTableNameSet = new LinkedHashSet<>(appTableNames);

        return jdbcTemplate.execute((ConnectionCallback<DatabaseRelationships>) connection -> {
            DatabaseMetaData metaData = connection.getMetaData();
            Map<String, Set<String>> parentsByChild = new LinkedHashMap<>();
            Map<String, Set<String>> childrenByParent = new LinkedHashMap<>();

            for (String tableName : appTableNames) {
                parentsByChild.put(tableName, new LinkedHashSet<>());
                childrenByParent.put(tableName, new LinkedHashSet<>());
            }

            for (String childTableName : appTableNames) {
                for (String parentTableName : readImportedKeys(metaData, childTableName)) {
                    if (!appTableNameSet.contains(parentTableName)) {
                        continue;
                    }

                    addRelationship(parentTableName, childTableName, parentsByChild, childrenByParent);
                }
            }

            applyManualRelationships(appTableNameSet, parentsByChild, childrenByParent);
            return new DatabaseRelationships(parentsByChild, childrenByParent);
        });
    }

    private void applyManualRelationships(
            Set<String> appTableNames,
            Map<String, Set<String>> parentsByChild,
            Map<String, Set<String>> childrenByParent
    ) {
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "user_accounts");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "diary_entry");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "diary_entry_goal");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "day_template");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "week_template");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "week_goal");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "user_food");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "user_tag");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "login_event");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "registration_event");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "refresh_token");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "tag");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "tag_usage_agg");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "users", "metric_usage_agg");

        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "general_food");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "user_food");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "metric_name_unit_link");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "entry_metric");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "entry_metric_goal");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "entry_metric_value");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "entry_metric_value_goal");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "entry_template_metric");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "entry_template_metric_value");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "dictionary_item", "metric_usage_agg");

        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "tag", "diary_entry_tag");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "tag", "user_tag");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "tag", "tag_chart_type_link");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "tag", "tag_usage_agg");

        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "diary_entry", "diary_entry_tag");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "diary_entry", "diary_entry_goal");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "diary_entry", "entry_metric");

        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "diary_entry_goal", "entry_metric_goal");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "entry_metric_goal", "entry_metric_value_goal");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "entry_metric", "entry_metric_value");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "entry_metric", "entry_metric_goal");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "entry_metric_value", "entry_metric_value_goal");

        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "diary_entry_template", "entry_template_metric");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "diary_entry_template", "template_entry_item");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "entry_template_metric", "entry_template_metric_value");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "day_template", "template_day_item");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "day_template", "template_entry_item");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "week_template", "template_day_item");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "week_goal", "day_goal");
        addManualRelationship(appTableNames, parentsByChild, childrenByParent, "day_goal", "diary_entry_goal");
    }

    private void addManualRelationship(
            Set<String> appTableNames,
            Map<String, Set<String>> parentsByChild,
            Map<String, Set<String>> childrenByParent,
            String parentTableName,
            String childTableName
    ) {
        if (!appTableNames.contains(parentTableName) || !appTableNames.contains(childTableName)) {
            return;
        }

        addRelationship(parentTableName, childTableName, parentsByChild, childrenByParent);
    }

    private void addRelationship(
            String parentTableName,
            String childTableName,
            Map<String, Set<String>> parentsByChild,
            Map<String, Set<String>> childrenByParent
    ) {
        parentsByChild.get(childTableName).add(parentTableName);
        childrenByParent.get(parentTableName).add(childTableName);
    }

    private Set<String> readImportedKeys(DatabaseMetaData metaData, String tableName) throws SQLException {
        Set<String> importedTableNames = new LinkedHashSet<>();

        try (ResultSet resultSet = metaData.getImportedKeys(null, null, tableName)) {
            while (resultSet.next()) {
                String parentTableName = resultSet.getString("PKTABLE_NAME");
                if (parentTableName != null) {
                    importedTableNames.add(parentTableName.toLowerCase(Locale.ROOT));
                }
            }
        }

        return importedTableNames;
    }

    private boolean tableExists(String tableName) {
        return jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, tableName, null)) {
                return resultSet.next();
            }
        });
    }

    private String getDatabaseProductName() {
        return jdbcTemplate.execute((ConnectionCallback<String>) connection ->
                connection.getMetaData().getDatabaseProductName()
        );
    }

    private boolean isPostgreSql(String databaseProductName) {
        return databaseProductName != null
                && databaseProductName.toLowerCase(Locale.ROOT).contains("postgresql");
    }

    private boolean isSqlite(String databaseProductName) {
        return databaseProductName != null
                && databaseProductName.toLowerCase(Locale.ROOT).contains("sqlite");
    }

    private void reinitializeDesktopUserIfNeeded() {
        DesktopUserBootstrap desktopUserBootstrap = desktopUserBootstrapProvider.getIfAvailable();
        if (desktopUserBootstrap == null) {
            return;
        }

        desktopUserBootstrap.ensureDesktopUserReady();
    }

    private List<String> getAllTableNames() {
        return Arrays.stream(TableType.values())
                .map(TableType::getTableName)
                .toList();
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private record DatabaseRelationships(
            Map<String, Set<String>> parentsByChild,
            Map<String, Set<String>> childrenByParent
    ) {
    }
}
