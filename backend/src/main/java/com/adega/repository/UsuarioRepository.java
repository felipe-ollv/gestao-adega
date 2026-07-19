package com.adega.repository;

import com.adega.model.Adega;
import com.adega.model.PerfilUsuario;
import com.adega.model.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepositoryBase<Usuario, Long> {
    public Optional<Usuario> findByEmail(String email) {
        return find("lower(email)", email.toLowerCase()).firstResultOptional();
    }

    public Optional<Usuario> findByUuidAndAdega(UUID uuid, UUID adegaUuid) {
        return find("uuid = ?1 and adega.uuid = ?2", uuid, adegaUuid).firstResultOptional();
    }

    public List<Usuario> listByAdega(UUID adegaUuid) {
        return list("adega.uuid = ?1 order by nome", adegaUuid);
    }

    public List<Usuario> listActiveManagersByAdega(Adega adega) {
        return list(
                "adega = ?1 and perfil = ?2 and ativo = true order by dataCadastro",
                adega,
                PerfilUsuario.GESTOR
        );
    }
}
