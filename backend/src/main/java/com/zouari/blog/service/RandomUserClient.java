package com.zouari.blog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zouari.blog.model.RandomUserResponse;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class RandomUserClient {
    private static final String BASE_URL = "https://randomuser.me/api/";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RandomUserClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public RandomUserResponse fetchUsers(int page, int results) throws IOException, InterruptedException {
        String url = BASE_URL + "?results=" + results + "&page=" + page;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), RandomUserResponse.class);
        } else {
            throw new IOException("Failed to fetch users. Status code: " + response.statusCode());
        }
    }
}
