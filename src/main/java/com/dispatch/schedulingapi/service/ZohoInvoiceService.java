package com.dispatch.schedulingapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    // 3. Generate a draft invoice for a specific customer with dynamic data
    public String createDraftInvoice(String customerId, String serviceName, double price) {
        try {
            String accessToken = getFreshAccessToken();

            // Using Java Text Blocks to dynamically inject ID, Service Name, and Price
            String jsonPayload = """
                {
                    "customer_id": "%s",
                    "line_items": [
                        {
                            "name": "%s",
                            "rate": %.2f,
                            "quantity": 1
                        }
                    ]
                }
                """.formatted(customerId, serviceName, price);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.zohoapis.com/invoice/v3/invoices"))
                    .header("Authorization", "Zoho-oauthtoken " + accessToken)
                    .header("X-com-zoho-invoice-organizationid", orgId)
                    .header("Content-Type", "application/json") // Tell Zoho we are sending JSON
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            System.err.println("Zoho Invoice Error: " + e.getMessage());
            return "Failed to create invoice.";
        }
    }
    // 4. Create a new contact in Zoho and return their new Zoho ID
    public String createContact(String name, String email, String phone) {
        try {
            String accessToken = getFreshAccessToken();

            // THE FIX: Build the JSON payload dynamically using Java Maps
            Map<String, Object> contactPerson = new HashMap<>();
            contactPerson.put("first_name", name);
            contactPerson.put("phone", phone);

            // Only attach the email field to the JSON if the user actually provided one
            if (email != null && !email.trim().isEmpty()) {
                contactPerson.put("email", email);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("contact_name", name);
            payload.put("contact_persons", List.of(contactPerson));

            // Use Jackson to safely convert the Java Map into a perfect JSON string
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.zohoapis.com/invoice/v3/contacts"))
                    .header("Authorization", "Zoho-oauthtoken " + accessToken)
                    .header("X-com-zoho-invoice-organizationid", orgId)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode rootNode = objectMapper.readTree(response.body());

            // Safety check to catch any other Zoho errors and print them to the console
            if (rootNode.has("code") && rootNode.get("code").asInt() != 0) {
                throw new RuntimeException("Zoho Error: " + rootNode.get("message").asText());
            }

            return rootNode.path("contact").path("contact_id").asText();

        } catch (Exception e) {
            System.err.println("Zoho Contact Creation Error: " + e.getMessage());
            throw new RuntimeException("Failed to create customer in Zoho");
        }
    }
}