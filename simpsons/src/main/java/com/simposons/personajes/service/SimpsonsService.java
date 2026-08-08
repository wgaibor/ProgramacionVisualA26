package com.simposons.personajes.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simposons.personajes.model.Personaje;
import com.simposons.personajes.model.SimpsonsResponse;

public class SimpsonsService {
    private static final String BASE_URL = "https://thesimpsonsapi.com/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SimpsonsService() {
        httpClient = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<SimpsonsResponse> getCharacters(int page) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/characters?page=" + page;
                HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .GET()
                                        .timeout(Duration.ofSeconds(10))
                                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if(response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), SimpsonsResponse.class);
                } else {
                    throw new IOException("Error al obtener personaje, Codigo "+response.statusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<Personaje> getCharacterById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/characters/" + id;
                System.out.println(url);
                HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .GET()
                                        .timeout(Duration.ofSeconds(10))
                                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), Personaje.class);
                } else {
                    throw new IOException("Error al obtener personaje, Codigo "+response.statusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

}
