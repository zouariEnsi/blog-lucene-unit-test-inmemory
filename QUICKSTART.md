# Quick Start Guide

This guide will get you up and running with the Lucene demo application in under 5 minutes.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (optional, but recommended)

## Option 1: Quick Start with Docker (Recommended)

This is the fastest way to get started:

```bash
# 1. Clone the repository
git clone https://github.com/zouariEnsi/blog-lucene-unit-test-inmemory.git
cd blog-lucene-unit-test-inmemory

# 2. Build everything (includes Docker image)
mvn clean install

# 3. Run the Docker container
docker run -p 8080:8080 -p 9990:9990 blog-lucene-app:1.0.0-SNAPSHOT
```

**That's it!** The application is now running.

## Accessing the Application

Open your browser and navigate to:
- **Web UI**: http://localhost:8080/blog-lucene-app/index.html
- **API**: http://localhost:8080/blog-lucene-app/api/

## Using the Application

### 1. Index Some Data

In the web UI:
1. Click the **"Indexation"** tab (default)
2. Click **"START NEW INDEXATION"** button
3. Watch the progress bar fill up (takes ~30-60 seconds to index 5,000 users)
4. Wait for completion message

Or via command line:
```bash
curl -X POST http://localhost:8080/blog-lucene-app/api/indexation/start
```

### 2. Search Users

In the web UI:
1. Click the **"Search Users"** tab
2. Enter a name (e.g., "john", "smith", "maria")
3. Click **"Search"** or press Enter
4. View the results

Or via command line:
```bash
curl "http://localhost:8080/blog-lucene-app/api/search/users?name=john"
```

### 3. Compare Performance

See how much faster Lucene is compared to linear search:

```bash
curl "http://localhost:8080/blog-lucene-app/api/benchmark/compare?name=john"
```

Example output:
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

**Lucene is 15x faster!** 🚀

## Option 2: Run Without Docker

If you have WildFly installed locally:

```bash
# 1. Build the WAR file
mvn clean package

# 2. Copy to WildFly
cp backend/target/blog-lucene-app.war $WILDFLY_HOME/standalone/deployments/

# 3. Start WildFly
$WILDFLY_HOME/bin/standalone.sh
```

## Testing

Run the test suite to verify everything works:

```bash
mvn test
```

Expected output: **16 tests passing** (7 for Lucene, 9 for SimpleSearch)

## Next Steps

- Read the [full README](README.md) for detailed API documentation
- Check out the [blog article](article.md) for deep technical insights
- Explore the source code to learn how Lucene works
- Try different search queries to see the capabilities

## Common Issues

### Port 8080 Already in Use

If you get a port conflict error:

```bash
# Use different ports
docker run -p 9080:8080 -p 9091:9990 blog-lucene-app:1.0.0-SNAPSHOT

# Access at http://localhost:9080/blog-lucene-app/index.html
```

### Docker Build Fails

If Docker build fails, you can skip it:

```bash
mvn clean package -pl backend
```

Then deploy the WAR file manually to any Jakarta EE 10 server.

### Tests Fail

Make sure you have Java 17+:

```bash
java -version
# Should show 17 or higher
```

## Learn More

- **Architecture**: See [article.md](article.md) for detailed architecture diagrams
- **API Reference**: See [README.md](README.md) for complete API documentation
- **Performance Details**: See the "Performance Comparison" section in [article.md](article.md)

## Questions?

Open an issue on GitHub or check the documentation files:
- [README.md](README.md) - Complete reference
- [article.md](article.md) - Technical deep dive
- [QUICKSTART.md](QUICKSTART.md) - This file
