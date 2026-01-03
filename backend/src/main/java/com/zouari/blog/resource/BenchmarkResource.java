package com.zouari.blog.resource;

import com.zouari.blog.model.User;
import com.zouari.blog.service.LuceneIndexService;
import com.zouari.blog.service.SimpleSearchService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * REST endpoint for comparing search performance between Lucene and simple linear search
 */
@Path("/benchmark")
public class BenchmarkResource {
    private static final Logger LOGGER = Logger.getLogger(BenchmarkResource.class.getName());

    @Inject
    private LuceneIndexService luceneIndexService;
    
    @Inject
    private SimpleSearchService simpleSearchService;

    /**
     * Compare search performance between Lucene and simple search
     */
    @GET
    @Path("/compare")
    @Produces(MediaType.APPLICATION_JSON)
    public Response compareSearchPerformance(@QueryParam("name") String name) {
        if (name == null || name.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Query parameter 'name' is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        Map<String, Object> results = new HashMap<>();
        results.put("searchTerm", name);

        try {
            // Warm-up: run each search once to avoid cold start bias
            simpleSearchService.searchUsersByName(name);
            luceneIndexService.searchUsersByName(name);
            
            // Benchmark simple search
            long simpleStartTime = System.nanoTime();
            List<User> simpleResults = simpleSearchService.searchUsersByName(name);
            long simpleEndTime = System.nanoTime();
            long simpleTimeMs = (simpleEndTime - simpleStartTime) / 1_000_000;
            
            // Benchmark Lucene search
            long luceneStartTime = System.nanoTime();
            List<User> luceneResults = luceneIndexService.searchUsersByName(name);
            long luceneEndTime = System.nanoTime();
            long luceneTimeMs = (luceneEndTime - luceneStartTime) / 1_000_000;
            
            // Calculate performance improvement
            double speedup = simpleTimeMs > 0 ? (double) simpleTimeMs / luceneTimeMs : 0;
            
            // Build response
            Map<String, Object> simpleSearchStats = new HashMap<>();
            simpleSearchStats.put("resultsCount", simpleResults.size());
            simpleSearchStats.put("timeMs", simpleTimeMs);
            simpleSearchStats.put("method", "Linear Search (O(n))");
            
            Map<String, Object> luceneSearchStats = new HashMap<>();
            luceneSearchStats.put("resultsCount", luceneResults.size());
            luceneSearchStats.put("timeMs", luceneTimeMs);
            luceneSearchStats.put("method", "Lucene Inverted Index");
            
            results.put("simpleSearch", simpleSearchStats);
            results.put("luceneSearch", luceneSearchStats);
            results.put("speedupFactor", String.format("%.2fx", speedup));
            results.put("improvementPercentage", String.format("%.1f%%", (speedup - 1) * 100));
            results.put("totalDocuments", simpleSearchService.getUserCount());
            
            LOGGER.info(String.format("Benchmark for '%s': Simple=%dms, Lucene=%dms, Speedup=%.2fx", 
                name, simpleTimeMs, luceneTimeMs, speedup));
            
            return Response.ok(results).build();
            
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (Exception e) {
            LOGGER.severe("Error running benchmark: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error running benchmark: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
