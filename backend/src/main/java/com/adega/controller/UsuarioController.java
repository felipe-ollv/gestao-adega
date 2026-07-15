package com.adega.controller;

import com.adega.dto.UsuarioRequest;
import com.adega.dto.UsuarioResponse;
import com.adega.dto.UsuarioUpdateRequest;
import com.adega.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

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

    @PUT
    @Path("/{uuid}")
    public UsuarioResponse update(@PathParam("uuid") UUID uuid, @Valid UsuarioUpdateRequest request) {
        return usuarioService.update(uuid, request);
    }

    @DELETE
    @Path("/{uuid}")
    public void delete(@PathParam("uuid") UUID uuid) {
        usuarioService.delete(uuid);
    }
}
