package com.adega.service.email;

import com.adega.model.Adega;
import com.adega.model.AdegaMensalidade;
import com.adega.model.Produto;
import com.adega.model.StatusPagamento;
import com.adega.model.TipoNotificacaoEmail;
import com.adega.model.Usuario;
import com.adega.repository.AdegaMensalidadeRepository;
import com.adega.repository.AdegaRepository;
import com.adega.repository.NotificacaoEmailRepository;
import com.adega.repository.ProdutoRepository;
import com.adega.repository.UsuarioRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailNotificationScheduler {
    private static final Logger LOG = Logger.getLogger(EmailNotificationScheduler.class);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    ResendEmailService resendEmailService;

    @Inject
    EmailTemplateService emailTemplateService;

    @Inject
    AdegaRepository adegaRepository;

    @Inject
    AdegaMensalidadeRepository mensalidadeRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    NotificacaoEmailRepository notificacaoRepository;

    @ConfigProperty(name = "adega.email.notifications.enabled")
    boolean enabled;

    @ConfigProperty(name = "adega.email.due-warning-days")
    int dueWarningDays;

    @ConfigProperty(name = "adega.email.monthly-fee-display")
    String monthlyFeeDisplay;

    @ConfigProperty(name = "adega.app.base-url")
    String appBaseUrl;

    @ConfigProperty(name = "adega.payment.whatsapp-number")
    String whatsappNumber;

    @Scheduled(
            every = "${adega.email.notifications.poll.every}",
            delayed = "10s",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    @Transactional
    void processNotifications() {
        if (!enabled || !resendEmailService.isConfigured()) {
            return;
        }

        try {
            ensureCurrentBillingCycles();
            processPendingRegistrations();
            processMonthlyPayments();
            processLowStockProducts();
        } catch (RuntimeException exception) {
            LOG.error("Falha ao processar notificações de e-mail.", exception);
        }
    }

    private void ensureCurrentBillingCycles() {
        adegaRepository.listAll().forEach(mensalidadeRepository::createPendingCurrentCycle);
    }

    private void processPendingRegistrations() {
        for (Adega adega : adegaRepository.listAll()) {
            mensalidadeRepository.findRegistrationCycle(adega)
                    .filter(monthlyPayment -> monthlyPayment.status == StatusPagamento.PENDENTE)
                    .ifPresent(monthlyPayment -> sendPendingRegistration(adega));
        }
    }

    private void processMonthlyPayments() {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);

        for (AdegaMensalidade monthlyPayment : mensalidadeRepository.listAllOrdered()) {
            if (monthlyPayment.status == StatusPagamento.PAGO) {
                sendPaymentConfirmation(monthlyPayment);
                continue;
            }

            if (isPendingRegistration(monthlyPayment)) {
                continue;
            }

            long daysUntilDue = ChronoUnit.DAYS.between(today, monthlyPayment.dataVencimento);

            if (daysUntilDue < 0) {
                sendOverdueNotice(monthlyPayment, Math.abs(daysUntilDue));
            } else if (daysUntilDue <= dueWarningDays && wasCreatedBeforeToday(monthlyPayment)) {
                sendDueWarning(monthlyPayment, daysUntilDue);
            }
        }
    }

    private void processLowStockProducts() {
        for (Produto product : produtoRepository.listActive()) {
            boolean lowStock = product.quantidadeEstoqueUnidades <= product.alertaEstoqueUnidades;

            if (!lowStock && product.alertaEstoqueNotificado) {
                product.alertaEstoqueNotificado = false;
                product.alertaEstoqueCiclo++;
                continue;
            }

            if (!lowStock || product.alertaEstoqueNotificado) {
                continue;
            }

            Map<String, Object> variables = Map.of(
                    "adegaNome", product.adega.nome,
                    "produtoNome", product.nome,
                    "estoqueAtual", product.quantidadeEstoqueUnidades,
                    "limiteEstoque", product.alertaEstoqueUnidades,
                    "unidadesPorCaixa", product.unidadesPorCaixa,
                    "produtosUrl", appUrl("/produtos")
            );
            boolean sentToAll = sendToManagers(
                    product.adega,
                    TipoNotificacaoEmail.PRODUTO_ESTOQUE_BAIXO,
                    product.uuid + ":" + product.alertaEstoqueCiclo,
                    EmailTemplate.PRODUTO_ESTOQUE_BAIXO,
                    variables
            );

            if (sentToAll) {
                product.alertaEstoqueNotificado = true;
            }
        }
    }

    private void sendPendingRegistration(Adega adega) {
        sendToManagers(
                adega,
                TipoNotificacaoEmail.CADASTRO_AGUARDANDO_PAGAMENTO,
                adega.uuid.toString(),
                EmailTemplate.CADASTRO_AGUARDANDO_PAGAMENTO,
                Map.of(
                        "adegaNome", adega.nome,
                        "statusPagamento", "Pendente",
                        "whatsappUrl", whatsappUrl(adega)
                )
        );
    }

    private void sendPaymentConfirmation(AdegaMensalidade monthlyPayment) {
        Instant paymentDate = monthlyPayment.dataPagamento != null
                ? monthlyPayment.dataPagamento
                : monthlyPayment.dataAtualizacao;

        sendToManagers(
                monthlyPayment.adega,
                TipoNotificacaoEmail.PAGAMENTO_CONFIRMADO,
                monthlyPayment.competencia.toString(),
                EmailTemplate.PAGAMENTO_CONFIRMADO,
                Map.of(
                        "adegaNome", monthlyPayment.adega.nome,
                        "statusPagamento", "Pago",
                        "periodoMensalidade", formatBillingPeriod(monthlyPayment),
                        "dataPagamento", formatDateTime(paymentDate),
                        "dataVencimento", formatDate(monthlyPayment.dataVencimento),
                        "loginUrl", appUrl("/entrar")
                )
        );
    }

    private void sendDueWarning(AdegaMensalidade monthlyPayment, long daysUntilDue) {
        sendToManagers(
                monthlyPayment.adega,
                TipoNotificacaoEmail.AVISO_VENCIMENTO_MENSALIDADE,
                monthlyPayment.competencia.toString(),
                EmailTemplate.AVISO_VENCIMENTO_MENSALIDADE,
                Map.of(
                        "adegaNome", monthlyPayment.adega.nome,
                        "diasParaVencimento", daysUntilDue,
                        "periodoMensalidade", formatBillingPeriod(monthlyPayment),
                        "dataVencimento", formatDate(monthlyPayment.dataVencimento),
                        "valorMensalidade", monthlyFeeDisplay,
                        "whatsappUrl", whatsappUrl(monthlyPayment.adega)
                )
        );
    }

    private void sendOverdueNotice(AdegaMensalidade monthlyPayment, long daysOverdue) {
        sendToManagers(
                monthlyPayment.adega,
                TipoNotificacaoEmail.MENSALIDADE_VENCIDA,
                monthlyPayment.competencia.toString(),
                EmailTemplate.MENSALIDADE_VENCIDA,
                Map.of(
                        "adegaNome", monthlyPayment.adega.nome,
                        "statusPagamento", "Pendente",
                        "periodoMensalidade", formatBillingPeriod(monthlyPayment),
                        "dataVencimento", formatDate(monthlyPayment.dataVencimento),
                        "diasEmAtraso", daysOverdue,
                        "whatsappUrl", whatsappUrl(monthlyPayment.adega)
                )
        );
    }

    private boolean sendToManagers(
            Adega adega,
            TipoNotificacaoEmail notificationType,
            String eventReference,
            EmailTemplate template,
            Map<String, Object> baseVariables
    ) {
        List<Usuario> managers = usuarioRepository.listActiveManagersByAdega(adega)
                .stream()
                .filter(manager -> isDeliverableAddress(manager.email))
                .toList();
        if (managers.isEmpty()) {
            return false;
        }

        boolean sentToAll = true;
        for (Usuario manager : managers) {
            String recipient = manager.email.trim().toLowerCase(Locale.ROOT);
            String reference = notificationReference(adega, notificationType, eventReference, recipient);
            String legacyReference = legacyNotificationReference(notificationType, eventReference, recipient);

            if (notificacaoRepository.existsByReference(reference)
                    || notificacaoRepository.existsByReference(legacyReference)) {
                continue;
            }

            Map<String, Object> variables = new HashMap<>(baseVariables);
            variables.put("destinatarioEmail", recipient);
            RenderedEmail email = emailTemplateService.render(template, variables);

            try {
                EmailSendResult result = resendEmailService.send(
                        recipient,
                        email,
                        reference
                );
                notificacaoRepository.record(
                        adega,
                        notificationType,
                        reference,
                        recipient,
                        result.providerId()
                );
            } catch (RuntimeException exception) {
                sentToAll = false;
                LOG.warnf(
                        exception,
                        "Não foi possível enviar %s para %s.",
                        notificationType,
                        recipient
                );
            }
        }
        return sentToAll;
    }

    private boolean isDeliverableAddress(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return !normalized.endsWith("@example.com")
                && !normalized.endsWith(".test")
                && !normalized.endsWith(".invalid")
                && !normalized.endsWith(".localhost");
    }

    static String notificationReference(
            Adega adega,
            TipoNotificacaoEmail notificationType,
            String eventReference,
            String recipient
    ) {
        return adega.uuid + ":" + notificationType.name() + ":" + eventReference + ":" + recipient;
    }

    static String legacyNotificationReference(
            TipoNotificacaoEmail notificationType,
            String eventReference,
            String recipient
    ) {
        return notificationType.name() + ":" + eventReference + ":" + recipient;
    }

    private boolean wasCreatedBeforeToday(AdegaMensalidade monthlyPayment) {
        LocalDate creationDate = monthlyPayment.dataCadastro.atZone(BUSINESS_ZONE).toLocalDate();
        return creationDate.isBefore(LocalDate.now(BUSINESS_ZONE));
    }

    private boolean isPendingRegistration(AdegaMensalidade monthlyPayment) {
        LocalDate registrationDate = monthlyPayment.adega.dataCadastro
                .atZone(BUSINESS_ZONE)
                .toLocalDate();
        return monthlyPayment.status == StatusPagamento.PENDENTE
                && monthlyPayment.competencia.equals(registrationDate);
    }

    private String whatsappUrl(Adega adega) {
        String number = whatsappNumber.replaceAll("\\D", "");
        String message = "Olá, a mensalidade de " + adega.nome
                + " está pendente na Gestão Comércio e gostaria de regularizar o acesso.";
        return "https://wa.me/" + number + "?text="
                + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }

    private String appUrl(String path) {
        return appBaseUrl.replaceAll("/+$", "") + path;
    }

    private String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    private String formatDateTime(Instant date) {
        return date.atZone(BUSINESS_ZONE).format(DATE_TIME_FORMAT);
    }

    private String formatBillingPeriod(AdegaMensalidade monthlyPayment) {
        return formatDate(monthlyPayment.competencia) + " a " + formatDate(monthlyPayment.dataVencimento);
    }
}
