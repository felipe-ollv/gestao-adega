package com.adega.service.email;

public record RenderedEmail(
        String subject,
        String html
) {
}
