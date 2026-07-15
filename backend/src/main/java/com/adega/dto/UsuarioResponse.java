package com.adega.dto;

import com.adega.model.PerfilUsuario;
import com.adega.model.Usuario;
import java.util.UUID;

public record UsuarioResponse(
        UUID uuid,
        String nome,
        String email,
        PerfilUsuario perfil,
        boolean ativo
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.uuid, usuario.nome, usuario.email, usuario.perfil, usuario.ativo);
    }
}
