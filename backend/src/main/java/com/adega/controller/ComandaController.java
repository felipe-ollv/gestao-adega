package com.adega.controller;

import com.adega.dto.AdicionarItemRequest;
import com.adega.dto.ComandaRequest;
import com.adega.dto.ComandaResponse;
import com.adega.dto.FecharComandaRequest;
import com.adega.model.StatusComanda;
import com.adega.service.ComandaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

@Path("/comandas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"GESTOR", "ATENDENTE"})
public class ComandaController {
    @Inject
    ComandaService comandaService;

    @GET
    public List<ComandaResponse> list(@QueryParam("status") StatusComanda status) {
        return comandaService.list(status);
    }

    @GET
    @Path("/{uuid}")
    public ComandaResponse get(@PathParam("uuid") UUID uuid) {
        return comandaService.get(uuid);
    }

    @POST
    public ComandaResponse open(@Valid ComandaRequest request) {
        return comandaService.open(request);
    }

    @POST
    @Path("/{uuid}/itens")
    public ComandaResponse addItem(@PathParam("uuid") UUID uuid, @Valid AdicionarItemRequest request) {
        return comandaService.addItem(uuid, request);
    }

    @PATCH
    @Path("/{uuid}/fechar")
    public ComandaResponse close(@PathParam("uuid") UUID uuid, @Valid FecharComandaRequest request) {
        return comandaService.close(uuid, request);
    }
}
