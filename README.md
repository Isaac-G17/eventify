# Eventify

🇬🇧 [Read this in English](README.en.md)

## Descripción

Eventify es una plataforma para gestionar un catálogo de eventos, lugares y categorías, con una API REST documentada y un panel administrativo web.
Incluye búsqueda con filtros y paginación, borrado lógico, validación de datos, errores estandarizados (RFC 7807) y una base de datos versionada con Flyway.

## ¿Qué es y qué problema resuelve?

Eventify es el sistema central con el que una organización publica y administra su oferta de eventos: qué evento es, cuándo ocurre, en qué lugar y ciudad se realiza y a qué categorías temáticas pertenece (conciertos, talleres, conferencias, deportes, etc.).

Resuelve los problemas típicos de manejar un catálogo grande a mano o con herramientas sueltas:

- **Gestión sin conocimientos técnicos:** los coordinadores registran, editan y eliminan eventos y lugares desde un panel web, sin depender de la API ni de la base de datos.
- **Encontrar rápido lo que se busca:** el catálogo se filtra por ciudad, categoría, capacidad del lugar y rango de fechas, con búsqueda parcial y sin importar mayúsculas, y se recorre por páginas sin degradar el tiempo de respuesta.
- **Datos confiables:** cada dato se valida antes de guardarse (por ejemplo, no se aceptan eventos sin nombre ni con fechas pasadas) y el sistema explica con claridad qué corregir, tanto en el panel como en la API.
- **Trazabilidad histórica:** los eventos eliminados no se borran físicamente, se desactivan (borrado lógico), así se conserva el historial para auditoría.
- **Integración segura con otros sistemas:** la API REST documentada en Swagger expone solo la información necesaria (no la estructura interna de la base de datos) y responde los errores en un formato estándar (RFC 7807) que cualquier aplicación puede procesar.
- **Base de datos versionada:** los cambios del esquema se aplican con migraciones de Flyway, así cualquier entorno queda igual al arrancar.

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.1 (Web MVC, Data JPA, Validation) |
| Base de datos | H2 (en archivo) + Flyway para las migraciones |
| Mapeo DTO | MapStruct 1.6.3 + Lombok |
| Vistas | Thymeleaf + Tailwind CSS |
| Documentación | springdoc-openapi 3.1.0 (Swagger UI) |
| Pruebas | JUnit 5, Mockito, MockMvc, `@DataJpaTest` |
| Build | Maven (incluye Maven Wrapper) |

## Estructura del proyecto

```
eventify/
├── src/main/java/com/eventify/eventify/
│   ├── controller/   # API REST (/api/**) y panel administrativo (/admin/**)
│   ├── docs/         # Ejemplos de errores para Swagger
│   ├── dto/          # DTOs de entrada/salida y Records de consulta
│   ├── exception/    # Excepciones de dominio y manejador global (ProblemDetail)
│   ├── mapper/       # Mappers de MapStruct
│   ├── model/        # Entidades JPA (Event, Venue, Category)
│   ├── repository/   # Repositorios Spring Data JPA
│   ├── service/      # Lógica de negocio
│   └── validation/   # Grupos de validación y @NoPastEvents
├── src/main/resources/
│   ├── db/migration/ # Migraciones Flyway V1 a V4 (incluye datos de ejemplo)
│   ├── templates/    # Vistas Thymeleaf
│   └── application.yaml
├── src/test/java/    # Pruebas
├── Semana 1 … Semana 4/  # Estado del proyecto al cierre de cada semana
├── mvnw / mvnw.cmd   # Maven Wrapper
└── pom.xml
```

## Instalación paso a paso

**Requisitos:** Java 21 (JDK) y Git. No hace falta instalar Maven ni una base de datos: el proyecto trae Maven Wrapper y usa H2.

1. Clona el repositorio y entra a la carpeta:

   ```bash
   git clone https://github.com/Isaac-G17/eventify.git
   ```

   ```bash
   cd eventify
   ```

2. Ejecuta las pruebas (opcional):

   - Linux / macOS / Git Bash:

     ```bash
     ./mvnw test
     ```

   - Windows (CMD / PowerShell):

     ```bash
     mvnw.cmd test
     ```

3. Inicia la aplicación:

   - Linux / macOS / Git Bash:

     ```bash
     ./mvnw spring-boot:run
     ```

   - Windows (CMD / PowerShell):

     ```bash
     mvnw.cmd spring-boot:run
     ```

   Al arrancar, Flyway crea la base de datos en `./data/eventify` y aplica las migraciones V1 a V4, que cargan 20 lugares, 11 categorías y 200 eventos de ejemplo.

4. Abre en el navegador:

   | Sección | URL |
   |---|---|
   | Inicio | http://localhost:8080/ |
   | Panel de eventos | http://localhost:8080/admin/eventos |
   | Panel de lugares | http://localhost:8080/admin/venues |
   | Swagger UI | http://localhost:8080/swagger-ui/index.html |
   | Consola H2 | http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/eventify`, usuario: `sa`, sin contraseña) |

> **Nota:** si la carpeta `data/` tiene una base de datos creada por una versión anterior del proyecto (antes de Flyway), Flyway no podrá migrarla. Borra la carpeta `data/` y vuelve a iniciar la aplicación.

## Autor

**Isaac Guzman**
