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

### Option 1: Using Docker (Recommended)

After building, you can run the application using Docker:

```bash
# Build the entire project (includes frontend, backend, and Docker image)
mvn clean install

# Run the Docker container
docker run -p 8080:8080 -p 9990:9990 blog-lucene-app:1.0.0-SNAPSHOT
```

The application will be available at:
- **Frontend UI**: `http://localhost:8080/blog-lucene-app/index.html`
- **REST API**: `http://localhost:8080/blog-lucene-app/api/`
- **Management Console**: `http://localhost:9990/console` (admin/admin)

### Option 2: Deploy to WildFly Manually

If you have WildFly installed locally:

```bash
# Build the WAR file
mvn clean package

# Copy to WildFly deployments directory
cp backend/target/blog-lucene-app.war $WILDFLY_HOME/standalone/deployments/

# Start WildFly
$WILDFLY_HOME/bin/standalone.sh
```

## Testing the Application

### Automated Tests

Run the complete test suite:

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl backend

# Run a specific test class
mvn test -pl backend -Dtest=LuceneIndexServiceTest
```

### Manual Testing via API

#### 1. Check Health
```bash
curl http://localhost:8080/blog-lucene-app/api/health-check
```

Expected: `OK`

#### 2. Start Indexation
```bash
curl -X POST http://localhost:8080/blog-lucene-app/api/indexation/start
```

Expected response:
```json
{
  "status": "STARTED",
  "message": "Indexation job started successfully. Use /status to track progress."
}
```

#### 3. Check Indexation Status
```bash
curl http://localhost:8080/blog-lucene-app/api/indexation/status
```

Expected response (in progress):
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

Wait for completion (status becomes "COMPLETED").

#### 4. Search Users
```bash
# Search by name
curl "http://localhost:8080/blog-lucene-app/api/search/users?name=john"

# Search with partial match
curl "http://localhost:8080/blog-lucene-app/api/search/users?name=mit"

# Search with special characters (will be normalized)
curl "http://localhost:8080/blog-lucene-app/api/search/users?name=jose"
```

Expected response:
```json
[
  {
    "name": {
      "first": "John",
      "last": "Doe"
    },
    "email": "john.doe@example.com",
    "login": {
      "uuid": "abc-123",
      "username": "johndoe"
    }
  }
]
```

#### 5. Compare Performance (Benchmark)
```bash
# Run performance comparison between linear search and Lucene
curl "http://localhost:8080/blog-lucene-app/api/benchmark/compare?name=john"
```

Expected response:
```json
{
  "searchTerm": "john",
  "simpleSearch": {
    "resultsCount": 127,
    "timeMs": 45,
    "method": "Linear Search (O(n))"
  },
  "luceneSearch": {
    "resultsCount": 127,
    "timeMs": 3,
    "method": "Lucene Inverted Index"
  },
  "speedupFactor": "15.00x",
  "improvementPercentage": "1400.0%",
  "totalDocuments": 5000
}
```

### Manual Testing via Web UI

1. **Open the application**: Navigate to `http://localhost:8080/blog-lucene-app/index.html`

2. **Index Users**:
   - Click the "Indexation" tab
   - Click "START NEW INDEXATION" button
   - Watch the progress bar update automatically
   - Wait for completion (shows duration and total users)

3. **Search Users**:
   - Click the "Search Users" tab
   - Enter a name (e.g., "john", "smith", "mit")
   - Click "Search" or press Enter
   - View the results with names and emails

4. **Test Search Features**:
   - **Case-insensitive**: Try "JOHN", "john", "John" - all return same results
   - **Partial matching**: Try "mit" - matches "Smith"
   - **Normalized text**: Try "jose" - matches "José"
   - **Multiple fields**: Searches both first name and last name

### Performance Benchmarking

To measure and compare performance:

```bash
# Benchmark with common name
curl "http://localhost:8080/blog-lucene-app/api/benchmark/compare?name=john"

# Benchmark with rare name
curl "http://localhost:8080/blog-lucene-app/api/benchmark/compare?name=xavier"

# Benchmark with partial match
curl "http://localhost:8080/blog-lucene-app/api/benchmark/compare?name=mit"
```

The benchmark endpoint:
- Warms up both search methods
- Measures time in milliseconds
- Compares linear search vs Lucene
- Reports speedup factor and improvement percentage

### Load Testing

For load testing, use tools like Apache JMeter or `ab`:

```bash
# Using Apache Bench (ab)
ab -n 1000 -c 10 "http://localhost:8080/blog-lucene-app/api/search/users?name=john"
```

This sends 1000 requests with 10 concurrent connections.

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

### Search Users
- **URL**: `http://localhost:8080/blog-lucene-app/api/search/users?name=<query>`
- **Method**: GET
- **Description**: Searches for users by name (first or last) in the Lucene index. The search is case-insensitive, supports partial matching, and normalizes accented characters (e.g., "Bro" matches "Bröcker").
- **Query Parameters**:
  - `name` (required): The search query string
- **Response (Success - 200 OK)**:
  ```json
  [
    {
      "name": {
        "first": "John",
        "last": "Doe"
      },
      "email": "john.doe@example.com",
      "login": {
        "uuid": "abc-123",
        "username": "johndoe"
      },
      ...
    }
  ]
  ```
- **Response (Bad Request - 400)**:
  ```json
  {
    "error": "Query parameter 'name' is required"
  }
  ```
- **Response (Index Not Created - 500)**:
  ```json
  {
    "error": "Index not created. Please create index first."
  }
  ```
- **Features**:
  - **Case-insensitive**: Searches for "john", "JOHN", or "JoHn" return the same results
  - **Normalized text**: Searches for "Bro" will match "Bröcker" (ASCII folding)
  - **Partial matching**: Searches for "mit" will match "Smith"
  - **Multi-field**: Searches in both firstName and lastName fields

### Benchmark Search Performance
- **URL**: `http://localhost:8080/blog-lucene-app/api/benchmark/compare?name=<query>`
- **Method**: GET
- **Description**: Compares search performance between linear search and Lucene search for the same query. Returns timing metrics and speedup factor.
- **Query Parameters**:
  - `name` (required): The search query string to benchmark
- **Response (Success - 200 OK)**:
  ```json
  {
    "searchTerm": "john",
    "simpleSearch": {
      "resultsCount": 127,
      "timeMs": 45,
      "method": "Linear Search (O(n))"
    },
    "luceneSearch": {
      "resultsCount": 127,
      "timeMs": 3,
      "method": "Lucene Inverted Index"
    },
    "speedupFactor": "15.00x",
    "improvementPercentage": "1400.0%",
    "totalDocuments": 5000
  }
  ```
- **Response (Bad Request - 400)**:
  ```json
  {
    "error": "Query parameter 'name' is required"
  }
  ```
- **Use Cases**:
  - Performance testing and validation
  - Demonstrating Lucene's speed advantages
  - Comparing different query patterns

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
