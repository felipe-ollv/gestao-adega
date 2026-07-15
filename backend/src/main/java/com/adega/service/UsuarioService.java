package com.adega.service;

import com.adega.dto.UsuarioRequest;
import com.adega.dto.UsuarioResponse;
import com.adega.exception.BusinessException;
import com.adega.model.Adega;
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
}
