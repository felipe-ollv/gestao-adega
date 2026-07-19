package com.adega.service;

import com.adega.dto.AuthRegisterRequest;
import com.adega.dto.AuthResponse;
import com.adega.dto.LoginRequest;
import com.adega.exception.BusinessException;
import com.adega.model.Adega;
import com.adega.model.AdegaMensalidade;
import com.adega.model.PerfilUsuario;
import com.adega.model.StatusPagamento;
import com.adega.model.Usuario;
import com.adega.repository.AdegaRepository;
import com.adega.repository.AdegaMensalidadeRepository;
import com.adega.repository.AdegaPagamentoRepository;
import com.adega.repository.UsuarioRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {
    @Inject
    AdegaRepository adegaRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    AdegaPagamentoRepository adegaPagamentoRepository;

    @Inject
    AdegaMensalidadeRepository adegaMensalidadeRepository;

    @Inject
    TokenService tokenService;

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        String email = normalizeEmail(request.email());
        String documento = onlyDigits(request.cnpjCpf());

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("E-mail já cadastrado.");
        }
        if (adegaRepository.existsByCnpjCpf(documento)) {
            throw new BusinessException("Documento da adega já cadastrado.");
        }

        Adega adega = new Adega();
        adega.nome = request.nomeAdega().trim();
        adega.cnpjCpf = documento;
        adegaRepository.persist(adega);

        Usuario usuario = new Usuario();
        usuario.adega = adega;
        usuario.nome = request.nomeUsuario().trim();
        usuario.email = email;
        usuario.senhaHash = BcryptUtil.bcryptHash(request.senha());
        usuario.perfil = PerfilUsuario.GESTOR;
        usuarioRepository.persist(usuario);

        adegaPagamentoRepository.createPending(adega);
        adegaMensalidadeRepository.createPendingCurrentMonth(adega);

        return responseFor(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizeEmail(request.email()))
                .filter(candidate -> candidate.ativo)
                .orElseThrow(() -> new BusinessException("Credenciais inválidas."));

        if (!BcryptUtil.matches(request.senha(), usuario.senhaHash)) {
            throw new BusinessException("Credenciais inválidas.");
        }

        return responseFor(usuario);
    }

    private AuthResponse responseFor(Usuario usuario) {
        AdegaMensalidade mensalidade = adegaMensalidadeRepository.currentMonthStatus(usuario.adega);
        StatusPagamento statusPagamento = mensalidade.status;
        String token = statusPagamento == StatusPagamento.PAGO
                ? tokenService.generate(usuario, statusPagamento, mensalidade.competencia, mensalidade.dataVencimento)
                : null;

        return new AuthResponse(
                token,
                usuario.uuid,
                usuario.adega.uuid,
                usuario.adega.nome,
                usuario.nome,
                usuario.perfil,
                statusPagamento,
                statusPagamento == StatusPagamento.PAGO,
                mensalidade.competencia,
                mensalidade.dataVencimento
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
