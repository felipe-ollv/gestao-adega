package com.adega.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ForbiddenOperationExceptionMapper implements ExceptionMapper<ForbiddenOperationException> {
    @Override
    public Response toResponse(ForbiddenOperationException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}
