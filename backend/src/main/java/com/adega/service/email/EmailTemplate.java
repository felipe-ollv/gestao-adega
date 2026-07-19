package com.adega.service.email;

public enum EmailTemplate {
    CADASTRO_AGUARDANDO_PAGAMENTO(
            "email-templates/cadastro-aguardando-pagamento.html",
            "{{adegaNome}}: cadastro criado - mensalidade pendente"
    ),
    PAGAMENTO_CONFIRMADO(
            "email-templates/pagamento-confirmado.html",
            "{{adegaNome}}: pagamento confirmado"
    ),
    AVISO_VENCIMENTO_MENSALIDADE(
            "email-templates/aviso-vencimento-mensalidade.html",
            "{{adegaNome}}: mensalidade vence em {{diasParaVencimento}} dia(s)"
    ),
    MENSALIDADE_VENCIDA(
            "email-templates/mensalidade-vencida.html",
            "{{adegaNome}}: mensalidade vencida"
    ),
    PRODUTO_ESTOQUE_BAIXO(
            "email-templates/produto-estoque-baixo.html",
            "{{adegaNome}}: estoque baixo - {{produtoNome}}"
    );

    public final String resourcePath;
    public final String subjectTemplate;

    EmailTemplate(String resourcePath, String subjectTemplate) {
        this.resourcePath = resourcePath;
        this.subjectTemplate = subjectTemplate;
    }
}
