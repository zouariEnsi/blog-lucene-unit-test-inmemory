package com.zouari.blog.resource;

import com.zouari.blog.model.User;
import com.zouari.blog.service.LuceneIndexService;

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

@Path("/search")
public class SearchResource {
    private static final Logger LOGGER = Logger.getLogger(SearchResource.class.getName());

    @Inject
    private LuceneIndexService luceneIndexService;

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchUsers(@QueryParam("name") String name) {
        if (name == null || name.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Query parameter 'name' is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        try {
            List<User> users = luceneIndexService.searchUsersByName(name.trim());
            return Response.ok(users).build();
        } catch (IllegalStateException e) {
            // Index not created yet
            LOGGER.warning("Index not created: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (Exception e) {
            // Other errors
            LOGGER.severe("Error searching users: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error searching users: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
