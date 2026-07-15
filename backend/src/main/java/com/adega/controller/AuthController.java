package com.adega.controller;

import com.adega.dto.AuthRegisterRequest;
import com.adega.dto.AuthResponse;
import com.adega.dto.LoginRequest;
import com.adega.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {
    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public AuthResponse register(@Valid AuthRegisterRequest request) {
        return authService.register(request);
    }

    @POST
    @Path("/login")
    public AuthResponse login(@Valid LoginRequest request) {
        return authService.login(request);
    }
}
