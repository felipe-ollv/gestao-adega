package com.adega.controller;

import com.adega.dto.ProdutoRequest;
import com.adega.dto.ProdutoResponse;
import com.adega.service.ProdutoService;
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

@Path("/produtos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"GESTOR", "ATENDENTE"})
public class ProdutoController {
    @Inject
    ProdutoService produtoService;

    @GET
    public List<ProdutoResponse> list() {
        return produtoService.list();
    }

    @POST
    @RolesAllowed("GESTOR")
    public ProdutoResponse create(@Valid ProdutoRequest request) {
        return produtoService.create(request);
    }

    @PUT
    @Path("/{uuid}")
    @RolesAllowed("GESTOR")
    public ProdutoResponse update(@PathParam("uuid") UUID uuid, @Valid ProdutoRequest request) {
        return produtoService.update(uuid, request);
    }

    @DELETE
    @Path("/{uuid}")
    @RolesAllowed("GESTOR")
    public void delete(@PathParam("uuid") UUID uuid) {
        produtoService.delete(uuid);
    }
}
