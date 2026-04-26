package com.example.activity_diary.platform.desktop.persistence;

import org.hibernate.community.dialect.SQLiteDialect;

import java.sql.Types;

/**
 * Hibernate maps Java {@link Long} identifiers to BIGINT, while SQLite exposes
 * auto-increment primary keys through INTEGER affinity. Treat them as
 * equivalent during schema validation so Liquibase can own the schema and
 * Hibernate can still validate it.
 */
public class DesktopSQLiteDialect extends SQLiteDialect {

    @Override
    public boolean equivalentTypes(int typeCode1, int typeCode2) {
        return super.equivalentTypes(typeCode1, typeCode2)
                || isBigIntIntegerPair(typeCode1, typeCode2);
    }

    private boolean isBigIntIntegerPair(int typeCode1, int typeCode2) {
        return (typeCode1 == Types.BIGINT && typeCode2 == Types.INTEGER)
                || (typeCode1 == Types.INTEGER && typeCode2 == Types.BIGINT);
    }
}
