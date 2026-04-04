package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.service.admin.AdminDatabaseService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDatabaseServiceImpl implements AdminDatabaseService {

    private static final String LIST_TABLES_SQL = """
            SELECT tablename
            FROM pg_tables
            WHERE schemaname = 'public'
            ORDER BY tablename
            """;
    private static final String LIST_FOREIGN_KEYS_SQL = """
            SELECT child.relname AS child_table, parent.relname AS parent_table
            FROM pg_constraint constraint_def
            JOIN pg_class child ON child.oid = constraint_def.conrelid
            JOIN pg_namespace child_namespace ON child_namespace.oid = child.relnamespace
            JOIN pg_class parent ON parent.oid = constraint_def.confrelid
            JOIN pg_namespace parent_namespace ON parent_namespace.oid = parent.relnamespace
            WHERE constraint_def.contype = 'f'
              AND child_namespace.nspname = 'public'
              AND parent_namespace.nspname = 'public'
            """;
    private static final List<String> EXCLUDED_TABLES = List.of(
            "databasechangelog",
            "databasechangeloglock",
            "dictionary_item",
            "users",
            "user_accounts"
    );

    private final EntityManager entityManager;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public int clearAllTables() {
        List<String> tables = entityManager.createNativeQuery(LIST_TABLES_SQL)
                .getResultList();
        Set<String> protectedTables = resolveProtectedTables();

        int clearedTables = 0;

        for (String table : tables) {
            if (protectedTables.contains(table)) {
                continue;
            }

            entityManager.createNativeQuery(
                    "TRUNCATE TABLE public." + quoteIdentifier(table) + " RESTART IDENTITY CASCADE"
            ).executeUpdate();
            clearedTables++;
        }

        return clearedTables;
    }

    @SuppressWarnings("unchecked")
    private Set<String> resolveProtectedTables() {
        List<Object[]> foreignKeys = entityManager.createNativeQuery(LIST_FOREIGN_KEYS_SQL)
                .getResultList();

        Map<String, List<String>> parentsByChild = foreignKeys.stream()
                .collect(Collectors.groupingBy(
                        row -> row[0].toString(),
                        Collectors.mapping(row -> row[1].toString(), Collectors.toList())
                ));

        Set<String> protectedTables = new LinkedHashSet<>(EXCLUDED_TABLES);
        ArrayDeque<String> queue = new ArrayDeque<>(EXCLUDED_TABLES);

        while (!queue.isEmpty()) {
            String childTable = queue.removeFirst();
            for (String parentTable : parentsByChild.getOrDefault(childTable, List.of())) {
                if (protectedTables.add(parentTable)) {
                    queue.addLast(parentTable);
                }
            }
        }

        return protectedTables;
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
