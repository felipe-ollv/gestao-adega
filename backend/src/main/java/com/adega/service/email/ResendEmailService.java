package com.adega.service.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ResendEmailService {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "resend.api.url")
    URI apiUrl;

    @ConfigProperty(name = "resend.api.key")
    Optional<String> apiKey;

    @ConfigProperty(name = "resend.from.email")
    String fromEmail;

    @ConfigProperty(name = "resend.reply-to.email")
    Optional<String> replyToEmail;

    public boolean isConfigured() {
        return apiKey.filter(value -> !value.isBlank()).isPresent();
    }

    public EmailSendResult send(String recipient, RenderedEmail email, String idempotencyKey) {
        String key = apiKey.filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalStateException("RESEND_API_KEY não configurada."));

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("from", fromEmail);
        payload.putArray("to").add(recipient);
        payload.put("subject", email.subject());
        payload.put("html", email.html());
        replyToEmail.filter(value -> !value.isBlank()).ifPresent(value -> payload.put("reply_to", value));

        try {
            HttpRequest request = HttpRequest.newBuilder(apiUrl)
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", idempotencyKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Resend recusou o envio com status " + response.statusCode() + ": " + response.body()
                );
            }

            JsonNode responseBody = objectMapper.readTree(response.body());
            return new EmailSendResult(responseBody.path("id").asText(null));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Envio de e-mail interrompido.", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Falha ao comunicar com o Resend.", exception);
        }
    }
}
