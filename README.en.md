# Eventify

🇪🇸 [Leer en español](README.md)

## Description

Eventify is a platform for managing a catalog of events, venues and categories, with a documented REST API and a web admin panel.
It includes filtered and paginated search, soft delete, input validation, standardized errors (RFC 7807) and a database versioned with Flyway.

## What is it and what problem does it solve?

Eventify is the central system an organization uses to publish and manage its event offering: what each event is, when it happens, which venue and city hosts it, and which themed categories it belongs to (concerts, workshops, conferences, sports, etc.).

It solves the usual problems of managing a large catalog by hand or with scattered tools:

- **Management without technical skills:** coordinators create, edit and delete events and venues from a web panel, without relying on the API or the database.
- **Finding things fast:** the catalog can be filtered by city, category, venue capacity and date range, with partial and case-insensitive search, and browsed page by page without slowing down.
- **Reliable data:** every field is validated before it is saved (for example, events without a name or with past dates are rejected) and the system clearly explains what to fix, both in the panel and in the API.
- **Historical traceability:** deleted events are not physically removed but deactivated (soft delete), so the history is kept for auditing.
- **Safe integration with other systems:** the REST API, documented in Swagger, exposes only the information needed (not the internal database structure) and returns errors in a standard format (RFC 7807) that any application can process.
- **Versioned database:** schema changes are applied through Flyway migrations, so every environment ends up identical on startup.

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 (Web MVC, Data JPA, Validation) |
| Database | H2 (file-based) + Flyway for migrations |
| DTO mapping | MapStruct 1.6.3 + Lombok |
| Views | Thymeleaf + Tailwind CSS |
| Documentation | springdoc-openapi 3.1.0 (Swagger UI) |
| Testing | JUnit 5, Mockito, MockMvc, `@DataJpaTest` |
| Build | Maven (Maven Wrapper included) |

## Project structure

```
eventify/
├── src/main/java/com/eventify/eventify/
│   ├── controller/   # REST API (/api/**) and admin panel (/admin/**)
│   ├── docs/         # Error examples for Swagger
│   ├── dto/          # Input/output DTOs and query Records
│   ├── exception/    # Domain exceptions and global handler (ProblemDetail)
│   ├── mapper/       # MapStruct mappers
│   ├── model/        # JPA entities (Event, Venue, Category)
│   ├── repository/   # Spring Data JPA repositories
│   ├── service/      # Business logic
│   └── validation/   # Validation groups and @NoPastEvents
├── src/main/resources/
│   ├── db/migration/ # Flyway migrations V1 to V4 (includes sample data)
│   ├── templates/    # Thymeleaf views
│   └── application.yaml
├── src/test/java/    # Tests
├── Semana 1 … Semana 4/  # Project state at the end of each week
├── mvnw / mvnw.cmd   # Maven Wrapper
└── pom.xml
```

## Step-by-step installation

**Requirements:** Java 21 (JDK) and Git. You don't need to install Maven or a database: the project ships with the Maven Wrapper and uses H2.

1. Clone the repository and enter the folder:

   ```bash
   git clone https://github.com/Isaac-G17/eventify.git
   ```

   ```bash
   cd eventify
   ```

2. Run the tests (optional):

   - Linux / macOS / Git Bash:

     ```bash
     ./mvnw test
     ```

   - Windows (CMD / PowerShell):

     ```bash
     mvnw.cmd test
     ```

3. Start the application:

   - Linux / macOS / Git Bash:

     ```bash
     ./mvnw spring-boot:run
     ```

   - Windows (CMD / PowerShell):

     ```bash
     mvnw.cmd spring-boot:run
     ```

   On startup, Flyway creates the database at `./data/eventify` and applies migrations V1 to V4, which load 20 venues, 11 categories and 200 sample events.

4. Open in your browser:

   | Section | URL |
   |---|---|
   | Home | http://localhost:8080/ |
   | Events panel | http://localhost:8080/admin/eventos |
   | Venues panel | http://localhost:8080/admin/venues |
   | Swagger UI | http://localhost:8080/swagger-ui/index.html |
   | H2 console | http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/eventify`, user: `sa`, no password) |

> **Note:** if the `data/` folder contains a database created by an earlier version of the project (before Flyway), Flyway won't be able to migrate it. Delete the `data/` folder and start the application again.

## Author

**Isaac Guzman**
