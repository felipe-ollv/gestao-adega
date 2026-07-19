# Templates de e-mail

Os templates usam placeholders no formato `{{nomeDaVariavel}}` e sao renderizados por `EmailTemplateService`.

## CADASTRO_AGUARDANDO_PAGAMENTO

- `adegaNome`
- `statusPagamento`
- `periodoMensalidade`
- `dataVencimento`
- `whatsappUrl`
- `destinatarioEmail`

## PAGAMENTO_CONFIRMADO

- `adegaNome`
- `statusPagamento`
- `periodoMensalidade`
- `dataPagamento`
- `dataVencimento`
- `loginUrl`
- `destinatarioEmail`

## AVISO_VENCIMENTO_MENSALIDADE

- `adegaNome`
- `diasParaVencimento`
- `periodoMensalidade`
- `dataVencimento`
- `valorMensalidade`
- `whatsappUrl`
- `destinatarioEmail`

## MENSALIDADE_VENCIDA

- `adegaNome`
- `statusPagamento`
- `periodoMensalidade`
- `dataVencimento`
- `diasEmAtraso`
- `whatsappUrl`
- `destinatarioEmail`

## PRODUTO_ESTOQUE_BAIXO

- `adegaNome`
- `produtoNome`
- `estoqueAtual`
- `limiteEstoque`
- `unidadesPorCaixa`
- `produtosUrl`
- `destinatarioEmail`

## Variavel automatica

- `anoAtual`

## Envio automatico

O `EmailNotificationScheduler` verifica os eventos a cada 60 segundos e envia somente para usuarios gestores ativos. Cada envio confirmado e registrado em `notificacao_email`, evitando duplicidade por evento e destinatario.

O alerta de estoque e rearmado depois que o estoque fica acima do limite configurado. Se o produto voltar a atingir o limite em outro momento, um novo e-mail pode ser enviado.

## Configuracao

- `RESEND_API_KEY`: chave de envio do Resend.
- `RESEND_FROM_EMAIL`: remetente de um dominio verificado.
- `RESEND_REPLY_TO_EMAIL`: endereco opcional para respostas.
- `APP_BASE_URL`: URL publica usada nos botoes dos e-mails.
- `EMAIL_MONTHLY_FEE_DISPLAY`: valor ou texto exibido no aviso de vencimento.
- `EMAIL_SCHEDULER_ENABLED`: habilita ou desabilita os gatilhos automaticos.
- `EMAIL_NOTIFICATION_POLL_EVERY`: intervalo de verificacao, por padrao `60s`.
- `EMAIL_DUE_WARNING_DAYS`: antecedencia do aviso de vencimento, por padrao 5 dias.
