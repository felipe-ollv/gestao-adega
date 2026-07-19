package com.adega.service.email;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EmailTemplateService {
    public RenderedEmail render(EmailTemplate template, Map<String, ?> variables) {
        Map<String, Object> values = new HashMap<>(variables);
        values.putIfAbsent("anoAtual", Year.now().getValue());

        return new RenderedEmail(
                renderString(template.subjectTemplate, values, false),
                renderString(loadTemplate(template.resourcePath), values, true)
        );
    }

    private String loadTemplate(String resourcePath) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Template de e-mail não encontrado: " + resourcePath);
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Erro ao carregar template de e-mail: " + resourcePath, exception);
        }
    }

    private String renderString(String template, Map<String, ?> variables, boolean html) {
        String rendered = template;

        for (Map.Entry<String, ?> entry : variables.entrySet()) {
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            rendered = rendered.replace(
                    "{{" + entry.getKey() + "}}",
                    html ? escapeHtml(value) : value
            );
        }

        return rendered.replaceAll("\\{\\{[^}]+}}", "");
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
