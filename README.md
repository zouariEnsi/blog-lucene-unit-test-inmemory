# blog-lucene-unit-test-inmemory

A blog project demonstrating how to test Apache Lucene search using in-memory index with unit tests.

## Project Structure

This is a multi-module Maven project with the following modules:

- **backend**: Jakarta EE 10 application packaged as WAR file
- **docker**: Module for building Docker images using io.fabric8 docker-maven-plugin

## Prerequisites

- Java 21
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

## API Endpoints

### Health Check
- **URL**: `http://localhost:8080/blog-lucene-app/api/health-check`
- **Method**: GET
- **Response**: `OK`

### Start Indexation
- **URL**: `http://localhost:8080/blog-lucene-app/api/indexation/start`
- **Method**: POST
- **Description**: Starts the indexation process that fetches 5000 users from randomuser.me API (50 pages × 100 users per page) and indexes them into Lucene
- **Response**: 
  ```json
  {
    "jobId": "uuid-string",
    "message": "Indexation started successfully"
  }
  ```

### Check Indexation Status
- **URL**: `http://localhost:8080/blog-lucene-app/api/indexation/status/{jobId}`
- **Method**: GET
- **Description**: Checks the status of an indexation job
- **Response**:
  ```json
  {
    "jobId": "uuid-string",
    "status": "IN_PROGRESS",
    "totalPages": 50,
    "processedPages": 25,
    "totalUsers": 2500,
    "message": "Processing page 25 of 50",
    "startTime": 1699999999999,
    "endTime": null
  }
  ```
- **Status Values**: 
  - `NOT_STARTED`: Job ID not found
  - `IN_PROGRESS`: Indexation is currently running
  - `COMPLETED`: Indexation finished successfully
  - `FAILED`: Indexation encountered an error

## Pattern Used: Asynchronous Job Pattern

The indexation functionality implements the **Asynchronous Job Pattern** (also known as **Long-Running Task Pattern** or **Command Pattern with Status Tracking**).

### How it works:
1. **Fire**: Client sends a POST request to `/indexation/start`
2. **Acknowledge**: Server immediately returns a job ID without waiting for completion
3. **Poll**: Client periodically checks the status using GET `/indexation/status/{jobId}`
4. **Complete**: Status eventually changes to `COMPLETED` or `FAILED`

This pattern is ideal for long-running operations that would timeout in a synchronous request-response model.

- **WildFly Management Console**: `http://localhost:9990/console`
  - Username: `admin`
  - Password: `admin`

## Technology Stack

- Jakarta EE 10
- Apache Lucene 9.11.1
- WildFly 31.0.1.Final (Java 21)
- Java 21
- Java EE 8 (Jakarta EE 8)
- Apache Lucene 9.11.1
- WildFly 25.0.0.Final
- Jackson 2.15.2 (JSON processing)
- Maven
- Docker (via io.fabric8 docker-maven-plugin)

## Features

- **REST Client**: Fetches random user data from https://randomuser.me/api/
- **Lucene Indexing**: Indexes user data into file-based Lucene index
- **Async Processing**: Background job execution using ExecutorService
- **Status Tracking**: Real-time progress monitoring

## Lucene Index Location

The Lucene index is stored at: `~/lucene-index/`

## Docker Module

The docker module uses the `io.fabric8:docker-maven-plugin` to build Docker images. The plugin is configured to:
- Pull the WildFly 31.0.1.Final base image (includes Java 21 support)
- Copy the WAR file to WildFly's deployment directory
- Expose ports 8080 (HTTP) and 9990 (management)
- Create default management user (admin/admin)
- Start WildFly server

## Jakarta EE 10 Migration

This project uses Jakarta EE 10, which requires:
- **Namespace change**: All `javax.*` imports are replaced with `jakarta.*`
- **WildFly 31+**: Minimum version that supports Jakarta EE 10
- **Java 21**: Required for WildFly 31 compatibility
- **Updated schemas**: web.xml uses Jakarta EE 6.0, beans.xml uses Jakarta EE 3.0

### Key Changes from Java EE 8
- `javax.ws.rs.*` → `jakarta.ws.rs.*`
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.ejb.*` → `jakarta.ejb.*`
- Jakarta EE Platform API updated from 8.0.0 to 10.0.0
