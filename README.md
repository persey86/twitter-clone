This is a RESTful API for a simplified Twitter clone built using Groovy, Spring Boot, MongoDB, Redis, JWT authentication and Docker.

## 🚀 Features

- User registration and login
- Follow/unfollow users
- Create, view, and delete posts
- Like and comment on posts
- View user feed (timeline)
- Redis-based caching for search
- JWT authentication and logout with token invalidation
- OpenAPI (Swagger) documentation
- Application monitoring with Prometheus

## 🧰 Tech Stack

- Groovy
- Spring Boot
- MongoDB
- Redis
- Spring Security + JWT
- Docker + Docker Compose
- Spock + Testcontainers
- Micrometer + Prometheus
- Springdoc OpenAPI

## 🗂 Project Structure

```
src
 └── main
     ├── groovy
     │   └── com.example.twitterclone
     │       ├── controller
     │       ├── model
     │       ├── repository
     │       ├── security
     │       ├── service
     │       └── config
     └── resources
         └── application.yml
```

## 📦 Build and Run

### Build
```bash
./gradlew clean build
```

### Run Locally
```bash
./gradlew bootRun
```

### Run with Docker
```bash
docker-compose up --build
```

## 🔐 Authentication

- JWT tokens are issued during login.
- Use the following header for authenticated requests:
  ```
  Authorization: Bearer <your_token>
  ```

## 🔍 API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## 🧪 Running Tests

```bash
./gradlew test
```

## 📁 Postman collections: [API_postman_collection.json](API_postman_collection.json)


## 📊 Monitoring

- Prometheus metrics: `http://localhost:8080/actuator/prometheus`
- Health check: `http://localhost:8080/actuator/health`

## 📁 Environment Variables

Some configurations can be overridden via:

- `application.yml`
- `SPRING_PROFILES_ACTIVE=test|prod`
- `spring.redis.host`, `spring.redis.port`
- `jwt.secret`, `jwt.expiration`

## 📌 Sample Endpoint: User Feed

```
GET /posts/user/{userId}
Authorization: Bearer <jwt_token>
```

## 🧾 TODO

- [ ] Notifications
- [ ] Reactions to comments
- [ ] Frontend web interface
- [ ] Rate limiting & abuse protection

---