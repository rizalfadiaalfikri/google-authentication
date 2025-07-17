package id.co.swamedia;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import id.co.swamedia.config.ConfigReader;

/**
 * Utility class for interacting with Google APIs, including retrieving user
 * data
 * using access tokens and verifying ID tokens from Google One Tap.
 */
public class GoogleApiClient {

    private static final Logger logger = LoggerFactory.getLogger(GoogleApiClient.class);

    /**
     * Sends a GET request to Google's user info endpoint using a provided access
     * token (from Google Sign-In button).
     *
     * @param accessToken The access token retrieved after user logs in via Google
     *                    button.
     * @return HttpResponse<String> containing the response body with user info or
     *         null if an error occurs.
     */
    public static HttpResponse<String> getButtonGoogleData(String accessToken) {
        try {
            // Build the target URL with the access token
            String targetUrl = Constants.GOOGLE_API + "?access_token=" + accessToken;

            // Create and send HTTP GET request
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .GET()
                        .header("Accept", "application/json")
                        .build();

                return client.send(request, HttpResponse.BodyHandlers.ofString());
            }

        } catch (IOException | InterruptedException e) {
            // Restore interrupt status if InterruptedException occurs
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifies and decodes a Google One Tap ID token.
     *
     * @param accessToken The ID token returned from Google One Tap (JWT format).
     * @return GoogleIdToken object if valid; null if invalid or an error occurs.
     */
    public static GoogleIdToken getOneTapGoogleData(String accessToken) {
        try {

            // Setup token verifier with your Google Client ID
            GoogleIdTokenVerifier verifier = getVerifier();
            if (verifier == null) {
                logger.error("Failed to create GoogleIdTokenVerifier.");
                return null;
            }

            // Verify and parse the ID token
            GoogleIdToken idToken = verifier.verify(accessToken);
            if (idToken == null) {
                logger.error("Invalid ID token.");
            }

            return idToken;

        } catch (Exception e) {
            logger.error("Error validating access token: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a GoogleIdTokenVerifier instance for verifying ID tokens.
     *
     * @param clientId Your Google Client ID.
     * @return GoogleIdTokenVerifier instance or null if an error occurs.
     */
    public static GoogleIdTokenVerifier getVerifier() {
        try {

            String configType = System.getProperty("config.type"); // "properties" atau "yaml"

            String clientId = null;
            if ("yaml".equalsIgnoreCase(configType)) {
                clientId = ConfigReader.getClientIdFromYaml("application.yml");
            } else {
                clientId = ConfigReader.getClientIdFromProperties("application.properties");
            }

            if (clientId == null || clientId.isEmpty()) {
                logger.error("Client ID not found in configuration.");
                return null;
            }

            // Initialize Google's HTTP and JSON parsing libraries
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            // Setup token verifier with your Google Client ID
            return new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                    .setAudience(Collections.singletonList(clientId))
                    .build();

        } catch (Exception e) {
            logger.error("Error creating token verifier: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}