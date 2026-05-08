# Activity Diary Backend

The **Activity Diary Backend** is the core service for the Activity Diary application. It provides a robust API for tracking daily activities, food intake, goals, and analytics. The project is designed with a dual-profile approach, supporting both a traditional web-based deployment (PostgreSQL) and a local desktop mode (SQLite).

## 🚀 Features

- **Activity Tracking**: Log daily entries, tags, and metrics.
- **Food & Nutrition**: Manage general and user-specific food databases.
- **Goals & Templates**: Set goals and use templates (day/week) for recurring activities.
- **Analytics**: Generate charts and aggregate data based on tags and metrics.
- **Multi-Profile Support**: 
  - `web`: Aimed at server deployments using PostgreSQL.
  - `desktop`: Aimed at local use with an embedded SQLite database.
- **Security**: JWT-based authentication and secure password handling.
- **Integrations**: Google API integration (Gmail) for notifications or messaging.

## 🛠 Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7
- **Database**: PostgreSQL (Web) / SQLite (Desktop)
- **Migration Tool**: Liquibase
- **Authentication**: JJWT (Java JWT)
- **Build Tool**: Gradle

## 🔗 Related Repositories

This repository is part of the Activity Diary ecosystem. Check out the other components:
- **Frontend**: [activity-diary-frontend](https://github.com/EAOErmak/activity-diary-frontend) - The web user interface.
- **Desktop Wrapper**: [activity-diary-desktop](https://github.com/EAOErmak/activity-diary-desktop) - Used for building the desktop version of the application.

---

## 🛠 Local `.env` setup

1. Copy [`.env.example`](./.env.example) to `.env`.
2. Replace placeholder values in `.env` with the real values from your IntelliJ IDEA Spring Boot run configuration.
3. If you want to remove the last IntelliJ-specific setting too, uncomment `spring.profiles.active=web` or `spring.profiles.active=desktop` in `.env`.
4. Keep secrets only in `.env` or `.env.local`. Do not commit them.

`spring.config.import` in [`src/main/resources/application.properties`](./src/main/resources/application.properties) loads `.env` and `.env.local` automatically. `.env.local` is optional and can override values from `.env`.

## Values to move from IntelliJ

Move these variables from the current IntelliJ run configuration into your local `.env`:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`
- `APP_ADMIN_DATABASE_CLEAR_ENABLED`
- `APP_BASE_URL`
- `CORS_ALLOWED_ORIGIN`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REFRESH_TOKEN`
- `GOOGLE_GMAIL_SENDER`

`APP_DB_PATH` is only needed if you want a custom SQLite location for the `desktop` profile. If you omit it, the desktop profile defaults to `${user.home}/.activity-diary/activity-diary.db`.

The current local IntelliJ run configuration also contains `REDIS_URL`, `TELEGRAM_BOT_NAME`, and `TELEGRAM_BOT_TOKEN`, but those names are not referenced anywhere in this backend project, so they were intentionally not added to `.env.example`.

## Running from IntelliJ IDEA

Use the existing Spring Boot run configuration or create a new one for `com.example.activity_diary.DiaryApplication`.

- Environment variables are no longer needed in the IntelliJ run configuration if `.env` exists in the project root.
- If `.env` contains `spring.profiles.active=web`, you can clear the IntelliJ `Active profiles` field entirely.
- The current local IntelliJ run configuration uses the `web` profile. Keep `web` if you want the same behavior after the migration.
- If you do not want to store the profile in `.env`, set the active profile in IntelliJ to `web` or `desktop`, pass a program argument such as `--spring.profiles.active=web`, or use an actual OS environment variable such as `SPRING_PROFILES_ACTIVE=web`.
- Keep the working directory pointed at the project root so Spring can resolve `.env`.

## Running from the terminal

Web profile:

```powershell
Copy-Item .env.example .env
.\gradlew.bat bootRun --args="--spring.profiles.active=web"
```

Desktop profile:

```powershell
Copy-Item .env.example .env
.\gradlew.bat bootRun --args="--spring.profiles.active=desktop"
```

You can also start the packaged jar with the same profile argument:

```powershell
java -jar build/libs/activity-diary-backend.jar --spring.profiles.active=web
```

## Profile notes

- Keep using `application.properties`, `application-web.properties`, and `application-desktop.properties` for profile structure.
- Use `.env` only to provide concrete values for externalized settings.
- `spring.profiles.active` inside `.env` is the right place for a local default profile if you want to avoid configuring `Active profiles` in IntelliJ.
- One `.env` is enough for the current project because the same base set of secrets and database values is reused by the active profile, while desktop-only overrides already have safe defaults in `application-desktop.properties`.

---

## 🤝 Contributing

Contributions are welcome! If you'd like to contribute, please follow these steps:

1. **Fork** the repository.
2. **Create a feature branch** (`git checkout -b feature/AmazingFeature`).
3. **Commit your changes** (`git commit -m 'Add some AmazingFeature'`).
4. **Push to the branch** (`git push origin feature/AmazingFeature`).
5. **Open a Pull Request**.

### Code Style
- Please follow standard Java coding conventions.
- Ensure all tests pass before submitting a pull request (`.\gradlew test`).

