# blog-lucene-unit-test-inmemory

A blog project demonstrating how to test Apache Lucene search using in-memory index with unit tests.

## Project Structure

This is a multi-module Maven project with the following modules:

- **frontend**: HTML5, CSS, and JavaScript frontend for managing indexation
- **backend**: Jakarta EE 10 application packaged as WAR file (includes frontend)
- **docker**: Module for building Docker images using io.fabric8 docker-maven-plugin

## Prerequisites

- Java 17 (minimum)
- Maven 3.6+
- Docker (for building/running Docker images)

## Building the Project

```bash
mvn clean install
```

This will:
1. Build the frontend module and package it as a ZIP file
2. Build the backend module and create a WAR file with embedded frontend
3. Build a Docker image with the WAR deployed to WildFly

## Running the Application

### Using Docker

After building, you can run the application using Docker:

```bash
docker run -p 8080:8080 -p 9990:9990 blog-lucene-app:1.0.0-SNAPSHOT
```

The application will be available at:
- **Frontend UI**: `http://localhost:8080/blog-lucene-app/index.html`
- **REST API**: `http://localhost:8080/blog-lucene-app/api/`
- **Management Console**: `http://localhost:9990/console` (admin/admin)

## Frontend Interface

The application includes a modern web interface for managing Lucene indexation:

### Features:
- **Visual Status Display**: Shows current index status with colored indicators
- **One-Click Indexation**: Start new indexation with a single button click
- **Real-Time Progress**: Progress bar and percentage updated every 10 seconds
- **History Tracking**: View details of the last indexation including duration
- **Responsive Design**: Works on desktop and mobile devices
- **Professional UI**: Modern gradient styling with animations

### Using the Frontend:
1. Navigate to `http://localhost:8080/blog-lucene-app/index.html`
2. Click "START NEW INDEXATION" to begin indexing users
3. Watch the progress update automatically every 10 seconds
4. View completion status and duration in the "Last Indexation" section

### Screenshots:

**Initial State:**
![Frontend Initial State](docs/screenshots/initial-state.png)

**Indexation In Progress:**
![Frontend In Progress](docs/screenshots/in-progress.png)

**Completed State:**
![Frontend Completed](docs/screenshots/completed.png)

## API Endpoints

### Health Check
- **URL**: `http://localhost:8080/blog-lucene-app/api/health-check`
- **Method**: GET
- **Response**: `OK`

### Start Indexation
- **URL**: `http://localhost:8080/blog-lucene-app/api/indexation/start`
- **Method**: POST
- **Description**: Starts an asynchronous indexation job that fetches 5000 users from randomuser.me API (50 pages × 100 users per page) and indexes them into Lucene. Only one indexation can run at a time.
- **Response (Success)**: 
  ```json
  {
    "status": "STARTED",
    "message": "Indexation job started successfully. Use /status to track progress."
  }
  ```
- **Response (Conflict - 400 Bad Request)**: 
  ```json
  {
    "error": "Indexation already in progress. Please wait until it finishes."
  }
  ```

### Check Indexation Status
- **URL**: `http://localhost:8080/blog-lucene-app/api/indexation/status`
- **Method**: GET
- **Description**: Checks the status of the current or last indexation job
- **Response (In Progress)**:
  ```json
  {
    "status": "IN_PROGRESS",
    "totalPages": 50,
    "processedPages": 25,
    "totalUsers": 2500,
    "message": "Processing page 25 of 50",
    "startTime": 1699999999999,
    "endTime": null,
    "durationFormatted": null
  }
  ```
  
- **Response (Completed)**:
  ```json
  {
    "status": "COMPLETED",
    "totalPages": 50,
    "processedPages": 50,
    "totalUsers": 5000,
    "message": "Indexation completed successfully. Total users indexed: 5000",
    "startTime": 1699999999999,
    "endTime": 1700000099999,
    "durationFormatted": "1min 40s"
  }
  ```
- **Status Values**: 
  - `NOT_STARTED`: No indexation has been run yet
  - `IN_PROGRESS`: Indexation is currently running
  - `COMPLETED`: Indexation finished successfully
  - `FAILED`: Indexation encountered an error

## Pattern Used: Asynchronous Job Pattern

The indexation functionality implements the **Asynchronous Job Pattern** with a **Single Job Constraint**.

### Key Features:
- **Single Job at a Time**: Only one indexation can run simultaneously. Attempting to start a second job while one is running returns a 400 Bad Request.
- **No UUID Required**: Simplified API without job IDs since there's only one job slot.
- **Human-Readable Duration**: Duration is displayed as "1min 45s" instead of milliseconds.

### How it works:
1. **Start**: Client sends a POST request to `/indexation/start`
2. **Acknowledge**: Server immediately returns success or error if already running
3. **Poll**: Client periodically checks the status using GET `/indexation/status`
4. **Complete**: Status eventually changes to `COMPLETED` or `FAILED`, then a new indexation can be started

This pattern is ideal for long-running operations that would timeout in a synchronous request-response model.

## Technology Stack

- Jakarta EE 10
- Apache Lucene 9.11.1
- WildFly 38.0.0.Final
- Java 17
- HTML5, CSS3, JavaScript (ES6+)
- Jackson 2.15.2 (JSON processing)
- Maven
- Docker (via io.fabric8 docker-maven-plugin)

## Features

- **Frontend Web UI**: Modern, responsive interface for managing indexation
- **REST Client**: Fetches random user data from https://randomuser.me/api/
- **Lucene Indexing**: Indexes user data into file-based Lucene index
- **Async Processing**: Background job execution using ExecutorService
- **Status Tracking**: Real-time progress monitoring with 10-second polling
- **Progress Visualization**: Animated progress bar with percentage display

## Lucene Index Location

The Lucene index is stored in the temporary directory: `${java.io.tmpdir}/lucene-index/`

## Docker Module

The docker module uses the `io.fabric8:docker-maven-plugin` to build Docker images. The plugin is configured to:
- Pull the WildFly 38.0.0.Final base image
- Copy the WAR file (with embedded frontend) to WildFly's deployment directory
- Expose ports 8080 (HTTP) and 9990 (management)
- Create default management user (admin/admin)
- Start WildFly server

## Frontend Module

The frontend module is built separately and packaged as a ZIP file using Maven Assembly Plugin. The ZIP contents are then unpacked into the backend WAR during the build process using Maven Dependency Plugin. This ensures the frontend files (HTML, CSS, JS) are served directly from the root of the web application.

### Build Process:
1. Frontend module creates `frontend-1.0.0-SNAPSHOT.zip` containing web resources
2. Backend module declares frontend as a dependency with `type=zip`
3. Maven Dependency Plugin unpacks the ZIP into the WAR during `prepare-package` phase
4. Final WAR contains both backend API and frontend UI

## Jakarta EE 10 Migration

This project uses Jakarta EE 10, which requires:
- **Namespace change**: All `javax.*` imports are replaced with `jakarta.*`
- **WildFly 38+**: Minimum version that supports Jakarta EE 10
- **Java 17+**: Required for WildFly 38 compatibility
- **Updated schemas**: web.xml uses Jakarta EE 6.0, beans.xml uses Jakarta EE 3.0

### Key Changes from Java EE 8
- `javax.ws.rs.*` → `jakarta.ws.rs.*`
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.ejb.*` → `jakarta.ejb.*`
- Jakarta EE Platform API updated from 8.0.0 to 10.0.0
