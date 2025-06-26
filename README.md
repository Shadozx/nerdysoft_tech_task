# Library Management System

REST API for managing books, members, and borrowings using Java, Spring Boot, JPA, Spring Validator, H2 database, JUnit and Mockito.

## How to run

1. Clone the repository:
```bash
   git clone https://github.com/Shadozx/nerdysoft_tech_task.git  
   cd nerdysoft_tech_task
```
2. Run the application:
```bash
   ./gradlew bootRun
```
   or run LibraryApplication.java from your IDE

3. Access:
```bash
    - Swagger UI: http://localhost:8080/swagger-ui.html
    - H2 Console: http://localhost:8080/h2-console  
      (JDBC URL: jdbc:h2:mem:librarydb, username: sa, password: sa)
```

## Tests

To run tests:
```bash
./gradlew test