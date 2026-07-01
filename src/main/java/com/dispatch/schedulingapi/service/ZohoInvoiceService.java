package com.dispatch.schedulingapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ZohoInvoiceService {

    @Value("${zoho.client.id}")
    private String clientId;

    @Value("${zoho.client.secret}")
    private String clientSecret;

    @Value("${zoho.refresh.token}")
    private String refreshToken;

    @Value("${zoho.org.id}")
    private String orgId;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. Automatically fetch a fresh 60-minute token
    private String getFreshAccessToken() throws Exception {
        String url = String.format(
                "https://accounts.zoho.com/oauth/v2/token?refresh_token=%s&client_id=%s&client_secret=%s&grant_type=refresh_token",
                refreshToken, clientId, clientSecret
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response to grab just the access_token
        JsonNode jsonNode = objectMapper.readTree(response.body());
        return jsonNode.get("access_token").asText();
    }

    // 2. Make a request to the Zoho API using the fresh token
    public String getCustomers() {
        try {
            String accessToken = getFreshAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.zohoapis.com/invoice/v3/contacts"))
                    .header("Authorization", "Zoho-oauthtoken " + accessToken)
                    .header("X-com-zoho-invoice-organizationid", orgId)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            System.err.println("Zoho API Error: " + e.getMessage());
            return "Failed to connect to Zoho.";
        }
    }
}