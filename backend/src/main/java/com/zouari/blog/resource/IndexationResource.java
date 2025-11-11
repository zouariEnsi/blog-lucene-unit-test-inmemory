package com.zouari.blog.resource;

import com.zouari.blog.model.IndexationStatus;
import com.zouari.blog.service.IndexationService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/indexation")
public class IndexationResource {

    @Inject
    private IndexationService indexationService;

    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startIndexation() {
        boolean started = indexationService.startIndexation();

        if (!started) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Indexation already in progress. Please wait until it finishes.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "STARTED");
        response.put("message", "Indexation job started successfully. Use /status to track progress.");
        return Response.ok(response).build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        IndexationStatus status = indexationService.getStatus();
        return Response.ok(status).build();
    }
}
