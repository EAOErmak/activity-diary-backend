package com.example.activity_diary.support;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class SqlCaptureStatementInspector implements StatementInspector {

    private static final List<String> STATEMENTS = new CopyOnWriteArrayList<>();

    @Override
    public String inspect(String sql) {
        if (sql != null) {
            String normalized = normalize(sql);
            if (isTracked(normalized)) {
                STATEMENTS.add(normalized);
            }
        }
        return sql;
    }

    public static void clear() {
        STATEMENTS.clear();
    }

    public static List<String> snapshot() {
        return new ArrayList<>(STATEMENTS);
    }

    public static long countSelects() {
        return STATEMENTS.stream()
                .filter(sql -> sql.startsWith("select "))
                .count();
    }

    public static long countStatements() {
        return STATEMENTS.size();
    }

    private static boolean isTracked(String sql) {
        if (sql.isBlank()) {
            return false;
        }

        return !sql.startsWith("pragma ")
                && !sql.startsWith("select tbl_name, sql from sqlite_master")
                && !sql.startsWith("select name from sqlite_master")
                && !sql.startsWith("select sql from sqlite_master");
    }

    private static String normalize(String sql) {
        return sql.replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
