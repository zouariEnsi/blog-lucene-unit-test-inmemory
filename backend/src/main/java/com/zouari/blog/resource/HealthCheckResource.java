package com.zouari.blog.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/health-check")
public class HealthCheckResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response healthCheck() {
        return Response.ok("OK").build();
    }
}
