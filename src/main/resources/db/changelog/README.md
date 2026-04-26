# Changelog Layout

- `db.changelog-master.xml` routes database-specific and shared changelogs.
- `postgres/db.postgres-master.xml` keeps the existing PostgreSQL migration history unchanged.
- `sqlite/db.sqlite-baseline.xml` bootstraps fresh desktop SQLite databases from the current schema baseline.
- `shared/db.shared-master.xml` is reserved for future database-agnostic changesets.

Rules for future migrations:

- Use Liquibase contexts only for the application profile: `web` and `desktop`.
- Use `dbms` for the database engine: `postgresql` and `sqlite`.
- PostgreSQL-only changesets must use `dbms="postgresql"`.
- SQLite-only changesets must use `dbms="sqlite"`.
- Shared migrations should normally have no `context` and no `dbms` when they are truly database-agnostic.
- For SQLite, prefer baseline/table-rebuild style changes over complex `ALTER TABLE` operations.
