package com.adega.service.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmailTemplateServiceTest {
    private final EmailTemplateService templateService = new EmailTemplateService();

    @Test
    void rendersAllTemplatesWithTheSharedLayoutAndExistingContent() {
        List<TemplateScenario> scenarios = List.of(
                new TemplateScenario(
                        EmailTemplate.CADASTRO_AGUARDANDO_PAGAMENTO,
                        Map.of(
                                "adegaNome", "Adega Central",
                                "statusPagamento", "Pendente",
                                "whatsappUrl", "https://example.com/whatsapp",
                                "destinatarioEmail", "gestor@example.com"
                        ),
                        "Adega Central: cadastro criado - mensalidade pendente",
                        "Cadastro criado",
                        "O comercio <strong>Adega Central</strong> foi cadastrado com sucesso.",
                        "Status:",
                        "https://example.com/whatsapp",
                        "Falar no WhatsApp"
                ),
                new TemplateScenario(
                        EmailTemplate.PAGAMENTO_CONFIRMADO,
                        Map.of(
                                "adegaNome", "Adega Central",
                                "statusPagamento", "Pago",
                                "periodoMensalidade", "Julho de 2026",
                                "dataPagamento", "24/07/2026 12:00",
                                "dataVencimento", "24/08/2026",
                                "loginUrl", "https://example.com/entrar",
                                "destinatarioEmail", "gestor@example.com"
                        ),
                        "Adega Central: pagamento confirmado",
                        "Pagamento confirmado",
                        "Recebemos o pagamento da mensalidade do comercio <strong>Adega Central</strong>.",
                        "Data do pagamento:",
                        "https://example.com/entrar",
                        "Acessar painel"
                ),
                new TemplateScenario(
                        EmailTemplate.AVISO_VENCIMENTO_MENSALIDADE,
                        Map.of(
                                "adegaNome", "Adega Central",
                                "diasParaVencimento", 5,
                                "periodoMensalidade", "Julho de 2026",
                                "dataVencimento", "29/07/2026",
                                "valorMensalidade", "R$ 99,90",
                                "whatsappUrl", "https://example.com/regularizar",
                                "destinatarioEmail", "gestor@example.com"
                        ),
                        "Adega Central: mensalidade vence em 5 dia(s)",
                        "Mensalidade perto do vencimento",
                        "A mensalidade do comercio <strong>Adega Central</strong> vence em <strong>5 dia(s)</strong>.",
                        "Valor:",
                        "https://example.com/regularizar",
                        "Regularizar mensalidade"
                ),
                new TemplateScenario(
                        EmailTemplate.MENSALIDADE_VENCIDA,
                        Map.of(
                                "adegaNome", "Adega Central",
                                "statusPagamento", "Pendente",
                                "periodoMensalidade", "Julho de 2026",
                                "dataVencimento", "23/07/2026",
                                "diasEmAtraso", 1,
                                "whatsappUrl", "https://example.com/whatsapp",
                                "destinatarioEmail", "gestor@example.com"
                        ),
                        "Adega Central: mensalidade vencida",
                        "Mensalidade vencida",
                        "A mensalidade do comercio <strong>Adega Central</strong> esta vencida.",
                        "Dias em atraso:",
                        "https://example.com/whatsapp",
                        "Falar no WhatsApp"
                ),
                new TemplateScenario(
                        EmailTemplate.PRODUTO_ESTOQUE_BAIXO,
                        Map.of(
                                "adegaNome", "Adega Central",
                                "produtoNome", "Vinho Tinto",
                                "estoqueAtual", 3,
                                "limiteEstoque", 5,
                                "unidadesPorCaixa", 6,
                                "produtosUrl", "https://example.com/produtos",
                                "destinatarioEmail", "gestor@example.com"
                        ),
                        "Adega Central: estoque baixo - Vinho Tinto",
                        "Estoque baixo",
                        "O produto <strong>Vinho Tinto</strong> esta com estoque baixo em <strong>Adega Central</strong>.",
                        "Limite configurado:",
                        "https://example.com/produtos",
                        "Ver produtos"
                )
        );

        for (TemplateScenario scenario : scenarios) {
            RenderedEmail rendered = templateService.render(scenario.template(), scenario.variables());
            String html = rendered.html();
            String normalizedHtml = html.toLowerCase();

            assertEquals(scenario.expectedSubject(), rendered.subject());
            assertTrue(html.contains(scenario.expectedTitle()));
            assertTrue(html.contains(scenario.expectedCopy()));
            assertTrue(html.contains(scenario.expectedDetailLabel()));
            assertTrue(html.contains("href=\"" + scenario.expectedActionUrl() + "\""));
            assertTrue(html.contains(scenario.expectedActionLabel()));
            assertTrue(html.contains("Mensagem enviada para gestor@example.com."));

            assertTrue(html.contains("background:#f0f2f5"));
            assertTrue(html.contains("background:#111827"));
            assertTrue(html.contains("background:#1A73E8"));
            assertTrue(html.contains("max-width:640px"));
            assertFalse(html.contains("{{"));
            assertFalse(normalizedHtml.contains("<img"));
            assertFalse(normalizedHtml.contains("<picture"));
            assertFalse(normalizedHtml.contains("<svg"));
            assertFalse(normalizedHtml.contains("background-image"));
            assertFalse(normalizedHtml.contains("data:image"));
        }
    }

    @Test
    void escapesDynamicValuesInHtmlWithoutChangingTheSubject() {
        RenderedEmail rendered = templateService.render(
                EmailTemplate.CADASTRO_AGUARDANDO_PAGAMENTO,
                Map.of(
                        "adegaNome", "Adega <Central> & Filhos",
                        "statusPagamento", "Pendente",
                        "whatsappUrl", "https://example.com/?a=1&b=2",
                        "destinatarioEmail", "gestor@example.com"
                )
        );

        assertEquals(
                "Adega <Central> & Filhos: cadastro criado - mensalidade pendente",
                rendered.subject()
        );
        assertTrue(rendered.html().contains("Adega &lt;Central&gt; &amp; Filhos"));
        assertTrue(rendered.html().contains("https://example.com/?a=1&amp;b=2"));
    }

    private record TemplateScenario(
            EmailTemplate template,
            Map<String, Object> variables,
            String expectedSubject,
            String expectedTitle,
            String expectedCopy,
            String expectedDetailLabel,
            String expectedActionUrl,
            String expectedActionLabel
    ) {
    }
}
