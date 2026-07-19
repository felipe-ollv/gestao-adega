package com.adega.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Provider
@Priority(Priorities.USER)
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOG = Logger.getLogger(ApiLoggingFilter.class);
    private static final String START_TIME_PROPERTY = ApiLoggingFilter.class.getName() + ".startTime";
    private static final String REQUEST_ID_PROPERTY = ApiLoggingFilter.class.getName() + ".requestId";
    private static final String REQUEST_BODY_PROPERTY = ApiLoggingFilter.class.getName() + ".requestBody";
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "authorization",
            "apikey",
            "api_key",
            "accesstoken",
            "access_token",
            "refreshtoken",
            "refresh_token",
            "token",
            "password",
            "senha",
            "secret",
            "cpf",
            "cnpj",
            "cpfcnpj",
            "cpf_cnpj"
    );

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "adega.api.logging.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "adega.api.logging.max-body-length", defaultValue = "8192")
    int maxBodyLength;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!enabled) {
            return;
        }

        requestContext.setProperty(START_TIME_PROPERTY, System.nanoTime());
        requestContext.setProperty(REQUEST_ID_PROPERTY, UUID.randomUUID().toString());

        if (!requestContext.hasEntity()) {
            requestContext.setProperty(REQUEST_BODY_PROPERTY, "-");
            return;
        }

        byte[] body = requestContext.getEntityStream().readAllBytes();
        requestContext.setEntityStream(new ByteArrayInputStream(body));
        requestContext.setProperty(
                REQUEST_BODY_PROPERTY,
                sanitize(new String(body, StandardCharsets.UTF_8))
        );
    }

    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext
    ) {
        if (!enabled) {
            return;
        }

        String requestId = property(requestContext, REQUEST_ID_PROPERTY, "-");
        String requestBody = property(requestContext, REQUEST_BODY_PROPERTY, "-");
        String responseBody = responseContext.hasEntity()
                ? sanitizeEntity(responseContext.getEntity())
                : "-";
        long durationMs = durationMs(requestContext.getProperty(START_TIME_PROPERTY));
        String endpoint = requestContext.getUriInfo().getRequestUri().getRawPath();

        LOG.infof(
                "API requestId=%s method=%s endpoint=%s status=%d durationMs=%d payload=%s response=%s",
                requestId,
                requestContext.getMethod(),
                endpoint,
                responseContext.getStatus(),
                durationMs,
                requestBody,
                responseBody
        );
    }

    private String sanitizeEntity(Object entity) {
        try {
            return truncate(objectMapper.writeValueAsString(maskSensitiveFields(objectMapper.valueToTree(entity))));
        } catch (RuntimeException | IOException exception) {
            return truncate(String.valueOf(entity));
        }
    }

    private String sanitize(String content) {
        if (content == null || content.isBlank()) {
            return "-";
        }

        try {
            return truncate(objectMapper.writeValueAsString(maskSensitiveFields(objectMapper.readTree(content))));
        } catch (IOException exception) {
            return truncate(content.replaceAll("[\\r\\n]+", " "));
        }
    }

    private JsonNode maskSensitiveFields(JsonNode node) {
        if (node == null) {
            return objectMapper.nullNode();
        }

        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node.deepCopy();
            object.fieldNames().forEachRemaining(fieldName -> {
                if (isSensitive(fieldName)) {
                    object.put(fieldName, "***");
                } else {
                    object.set(fieldName, maskSensitiveFields(object.get(fieldName)));
                }
            });
            return object;
        }

        if (node.isArray()) {
            ArrayNode array = objectMapper.createArrayNode();
            node.forEach(item -> array.add(maskSensitiveFields(item)));
            return array;
        }

        return node;
    }

    private boolean isSensitive(String fieldName) {
        return SENSITIVE_FIELDS.contains(fieldName.toLowerCase(Locale.ROOT).replace("-", ""));
    }

    private String truncate(String value) {
        int limit = Math.max(maxBodyLength, 256);
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...[truncated]";
    }

    private long durationMs(Object startTime) {
        if (startTime instanceof Long start) {
            return (System.nanoTime() - start) / 1_000_000;
        }
        return 0;
    }

    private String property(ContainerRequestContext context, String key, String defaultValue) {
        Object value = context.getProperty(key);
        return value == null ? defaultValue : String.valueOf(value);
    }
}
