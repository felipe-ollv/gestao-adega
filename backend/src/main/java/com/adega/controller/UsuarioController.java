package com.adega.controller;

import com.adega.dto.UsuarioRequest;
import com.adega.dto.UsuarioResponse;
import com.adega.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/usuarios")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("GESTOR")
public class UsuarioController {
    @Inject
    UsuarioService usuarioService;

    @GET
    public List<UsuarioResponse> list() {
        return usuarioService.list();
    }

    @POST
    public UsuarioResponse create(@Valid UsuarioRequest request) {
        return usuarioService.create(request);
    }
}
