package com.zouari.blog.resource;

import com.zouari.blog.model.IndexationStatus;
import com.zouari.blog.service.IndexationService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        try {
            String jobId = indexationService.startIndexation();
            Map<String, String> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("message", "Indexation started successfully");
            return Response.accepted(response).build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to start indexation: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @GET
    @Path("/status/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus(@PathParam("jobId") String jobId) {
        try {
            IndexationStatus status = indexationService.getStatus(jobId);
            return Response.ok(status).build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get status: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
