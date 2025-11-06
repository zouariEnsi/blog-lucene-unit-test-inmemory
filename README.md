# blog-lucene-unit-test-inmemory

A blog project demonstrating how to test Apache Lucene search using in-memory index with unit tests.

## Project Structure

This is a multi-module Maven project with the following modules:

- **backend**: Java EE 8 application packaged as WAR file
- **docker**: Module for building Docker images using io.fabric8 docker-maven-plugin

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Docker (for building/running Docker images)

## Building the Project

```bash
mvn clean install
```

This will:
1. Build the backend module and create a WAR file
2. Build a Docker image with the WAR deployed to WildFly

## Running the Application

### Using Docker

After building, you can run the application using Docker:

```bash
docker run -p 8080:8080 -p 9990:9990 blog-lucene-app:1.0.0-SNAPSHOT
```

### Endpoints

- **Health Check**: `http://localhost:8080/blog-lucene-app/api/health-check`
  - Returns: `OK`

## Technology Stack

- Java EE 8 (Jakarta EE 8)
- Apache Lucene 8.11.2
- WildFly 25.0.0.Final
- Maven
- Docker (via io.fabric8 docker-maven-plugin)

## Docker Module

The docker module uses the `io.fabric8:docker-maven-plugin` to build Docker images. The plugin is configured to:
- Pull the WildFly 25.0.0.Final base image
- Copy the WAR file to WildFly's deployment directory
- Expose ports 8080 (HTTP) and 9990 (management)
- Start WildFly server