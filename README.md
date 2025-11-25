# Paymeter Backend Software Engineer Assessment

## Deliverable

There is quite some flexibility in what can be delivered, just ensure the service:

* is a web service
* can run on Mac OS X or Linux (x86-64)
* is written in Java
* uses Spring Boot
* can make use of any existing open source libraries that don't directly address the problem statement (use your best judgement)

Send us:

* The full source code, including any code written which is not part of the normal program run (scripts, tests)
* Clear instructions on how to obtain and run the program
* Please provide any deliverables and instructions using a public Github (or similar) Repository as several people will need to inspect the solution

## Evaluation
The point of the exercise is for us to see some of the code you wrote (and should be proud of).
We will especially consider:

* Code organisation
* Quality
* Readability
* Actually solving the problem

## Deployment Guide

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Java | 17+ | Required for manual deployment |
| Gradle | 8.x | Included via wrapper (`./gradlew`) |
| Docker | 20.10+ | Required for containerized deployment |
| Docker Compose | 2.x | Required for full stack deployment |
| PostgreSQL | 13+ | Only for manual deployment (Docker Compose includes it) |

---

### Option 1: Docker Compose (Recommended)

This is the easiest way to run the full stack (application + PostgreSQL database with seed data).

#### Start the application

```shell
docker compose up --build
```

This command will:
1. Build the application using Gradle inside a container
2. Start PostgreSQL 13 with the `parking` database
3. Execute `db/init/001_create_pricing.sql` to create the `pricing` table and seed data
4. Start the Spring Boot application connected to the database

#### Stop the application

```shell
docker compose down
```

#### View logs

```shell
# All services
docker compose logs -f

# Only the application
docker compose logs -f app

# Only the database
docker compose logs -f db
```

#### Environment Variables (docker-compose.yml)

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `POSTGRES_DB` | `parking` | Database name |
| `POSTGRES_USER` | `postgres` | Database user |
| `POSTGRES_PASSWORD` | `postgres` | Database password |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/parking` | JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Spring datasource user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Spring datasource password |

#### Exposed Ports

| Service | Port | Description |
|---------|------|-------------|
| app | 8080 | HTTP API |
| db | 5432 | PostgreSQL |

---

### Option 2: Docker Only (Without Compose)

Build and run the application container manually. Requires an external PostgreSQL database.

```shell
# Build the image
docker build -t app .

# Run the container (configure your database connection)
docker run -it -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/parking \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  app
```

---

### Option 3: Manual Deployment (Local Development)

#### Prerequisites
- Java 17 installed and configured (`JAVA_HOME`)
- PostgreSQL 13+ running locally with:
  - Database: `parking` (or configure via environment variables)
  - Execute `db/init/001_create_pricing.sql` to create schema and seed data

#### Start the application

```shell
# Using Gradle wrapper (recommended)
./gradlew bootRun

# Or with custom database configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/parking \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
./gradlew bootRun
```

#### Build the JAR

```shell
./gradlew clean build
```

The JAR file will be generated at: `build/libs/assessment-0.0.1-SNAPSHOT.jar`

#### Run the JAR directly

```shell
java -jar build/libs/assessment-0.0.1-SNAPSHOT.jar
```

---

### Running Tests

```shell
# Local (uses H2 in-memory database)
./gradlew test

# Using Docker
docker run --rm -u gradle -v "$PWD":/home/gradle/project -w /home/gradle/project gradle:8-jdk17 gradle test

# Inside running Docker Compose environment
docker compose exec app gradle test
```

---

### Verify the Application is Running

```shell
# Health check
curl http://localhost:8080

# Expected response: ok
```

---

### Database Seed Data

The `db/init/001_create_pricing.sql` script creates the following parking configurations:

| parking_id | hourly_rate | max_cap | cap_window | first_hour_free |
|------------|-------------|---------|------------|-----------------|
| P000123 | 2 EUR | 15 EUR | 24 hours | No |
| P000456 | 3 EUR | 20 EUR | 12 hours | Yes |

## Challenge

Our customers want to be sure they're properly charging the correct amount on their parkings. 
For this reason, we plan to create a new pricing calculation feature so they can test multiple scenarios.
We have two customers with one parking each:

* Customer 1:
  * Parking id: `P000123`
  * Hourly price: 2€
  * Discounts
    * Max price per day: 15€

* Customer 2 
  * Parking id: `P000456`
  * Hourly price: 3€
  * Discounts
    * Max price every 12 hours: 20€
    * First hour is free

Note:
  * The price of a fraction of an hour is the same as a full hour
  * If duration of the stay is less than one minute, parking is free
  * There's no max time for a stay
  * There's no limit of times that max price discount can be applied
  * Max price discount starts counting when entering the parking 

Requirements:
* Endpoint: POST `/tickets/calculate`
* Request:
  * Content type: JSON
  * Fields:
    * `parkingId`: string (required)
    * `from`: ISO 8601 timestamp string (required)
    * `to`: ISO 8601 timestamp string (optional, defaults to current time)
* Response:
  * Content type: JSON
  * Fields:
    * `parkingId`: string (required)
    * `from`: ISO 8601 timestamp string (required)
    * `to`: ISO 8601 timestamp string (required)
    * `duration`: integer (minutes)
    * `price`: string (integer amount + currency code, e.g. 2.35€ would be `"235EUR"`)
  * Status codes:
    * 200 ok
    * 400 invalid request
    * 404 parking not found
    * 500 server error
    * (feel free to return any status codes needed)

Example usage:
```shell
curl --location 'http://localhost:8080/tickets/calculate' \
--header 'Content-Type: application/json' \
--data '{
    "parkingId": "P000456",
    "from": "2025-02-27T09:00:00",
    "to": "2025-02-28T09:10:00"
}'
```

Example response:
```json
{
  "parkingId": "P000123",
  "from": "2024-02-27T09:00:00Z",
  "to": "2024-02-27T10:00:00Z",
  "duration": 60,
  "price": "20EUR"
}
```

Error examples:
```shell
# invalid date
curl -X POST http://localhost:8080/tickets/calculate \
  -H "Content-Type: application/json" \
  -d '{"parkingId":"P000123","from":"bad-date"}'
# => 400 {"message":"Invalid date format","code":"BAD_REQUEST","status":400}
```
