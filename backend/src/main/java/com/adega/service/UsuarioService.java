package com.adega.service;

import com.adega.dto.UsuarioRequest;
import com.adega.dto.UsuarioResponse;
import com.adega.dto.UsuarioUpdateRequest;
import com.adega.exception.BusinessException;
import com.adega.model.Adega;
import com.adega.model.PerfilUsuario;
import com.adega.model.Usuario;
import com.adega.repository.AdegaRepository;
import com.adega.repository.UsuarioRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UsuarioService {
    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    AdegaRepository adegaRepository;

    @Inject
    SecurityService securityService;

    public List<UsuarioResponse> list() {
        return usuarioRepository.listByAdega(securityService.currentAdegaUuid())
                .stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        UUID adegaUuid = securityService.currentAdegaUuid();
        Adega adega = adegaRepository.findByUuid(adegaUuid)
                .orElseThrow(() -> new BusinessException("Adega não encontrada."));
        String email = request.email().trim().toLowerCase();

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.adega = adega;
        usuario.nome = request.nome().trim();
        usuario.email = email;
        usuario.senhaHash = BcryptUtil.bcryptHash(request.senha());
        usuario.perfil = request.perfil();
        usuarioRepository.persist(usuario);

        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public UsuarioResponse update(UUID uuid, UsuarioUpdateRequest request) {
        UUID adegaUuid = securityService.currentAdegaUuid();
        Usuario usuario = getByUuid(uuid, adegaUuid);
        String email = request.email().trim().toLowerCase();
        boolean isCurrentUser = usuario.uuid.equals(securityService.currentUsuarioUuid());
        boolean willDeactivate = !request.ativo();
        boolean willStopBeingGestor = usuario.perfil == PerfilUsuario.GESTOR && request.perfil() != PerfilUsuario.GESTOR;

        if (isCurrentUser && (willDeactivate || willStopBeingGestor)) {
            throw new BusinessException("Não é permitido remover seu próprio acesso de gestor.");
        }

        if ((willDeactivate || willStopBeingGestor) && usuario.perfil == PerfilUsuario.GESTOR) {
            ensureAnotherActiveGestor(usuario);
        }

        usuarioRepository.findByEmail(email)
                .filter(existing -> !existing.uuid.equals(usuario.uuid))
                .ifPresent(existing -> {
                    throw new BusinessException("E-mail já cadastrado.");
                });

        usuario.nome = request.nome().trim();
        usuario.email = email;
        usuario.perfil = request.perfil();
        usuario.ativo = request.ativo();

        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.senhaHash = BcryptUtil.bcryptHash(request.senha());
        }

        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void delete(UUID uuid) {
        UUID adegaUuid = securityService.currentAdegaUuid();
        Usuario usuario = getByUuid(uuid, adegaUuid);

        if (usuario.uuid.equals(securityService.currentUsuarioUuid())) {
            throw new BusinessException("Não é permitido excluir seu próprio usuário.");
        }

        if (usuario.perfil == PerfilUsuario.GESTOR && usuario.ativo) {
            ensureAnotherActiveGestor(usuario);
        }

        usuario.ativo = false;
    }

    private Usuario getByUuid(UUID uuid, UUID adegaUuid) {
        return usuarioRepository.findByUuidAndAdega(uuid, adegaUuid)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));
    }

    private void ensureAnotherActiveGestor(Usuario usuario) {
        boolean hasAnotherGestor = usuarioRepository.listByAdega(usuario.adega.uuid)
                .stream()
                .anyMatch(candidate ->
                        candidate.ativo
                                && candidate.perfil == PerfilUsuario.GESTOR
                                && !candidate.uuid.equals(usuario.uuid));

        if (!hasAnotherGestor) {
            throw new BusinessException("É necessário manter ao menos um gestor ativo.");
        }
    }
}
