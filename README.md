# Approval Flow Backend

A Java backend built with Spring Boot for managing approval workflows with email notifications.

## Features

- Manage approval requests (create, approve, reject)
- Email notifications for request updates
- User and request type management
- Request audit logging

## Tech Stack

- Java 21
- Spring Boot 4.0
- MySQL
- JPA/Hibernate
- JavaMail

## Project Structure

```
src/main/java/dev/danielcorrea/backbdb/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── model/          # Entities
└── dto/            # Data transfer objects
```

## API Endpoints

### Requests
- `POST /api/requests` - Create a new request
- `GET /api/requests` - List all requests
- `GET /api/requests/{id}` - Get request details
- `POST /api/requests/{id}/approve` - Approve a request
- `POST /api/requests/{id}/reject` - Reject a request

### Users
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user details

### Request Types
- `GET /api/request-types` - List all request types
- `GET /api/request-types/{id}` - Get request type details

## How to Start

1. Set up environment variables in a `.env` file:

   ```env
   DB_URL=jdbc:mysql://<host>:<port>/<database>
   DB_USERNAME=<username>
   DB_PASSWORD=<password>
   MAIL_HOST=<smtp_host>
   MAIL_PORT=<smtp_port>
   MAIL_USERNAME=<email>
   MAIL_PASSWORD=<email_password>
   ```

2. Run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

The API will be available at `http://localhost:8080`.