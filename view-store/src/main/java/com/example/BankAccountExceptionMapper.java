package com.example;

import com.example.exception.BankAccountNotFoundException;
import com.example.dto.ExceptionResponseDTO;
import com.fasterxml.jackson.core.JsonParseException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.LocalDateTime;

public class BankAccountExceptionMapper {

    private final static Logger logger = Logger.getLogger(BankAccountExceptionMapper.class);

    @ServerExceptionMapper(priority = 1)
    public RestResponse<ExceptionResponseDTO> mapBankAccountNotFoundException(BankAccountNotFoundException ex) {
        final var response = new ExceptionResponseDTO(ex.getMessage(), Response.Status.NOT_FOUND.getStatusCode(), LocalDateTime.now());
        logger.errorv(ex, "(mapBankAccountNotFoundException) response: {0}", response);
        return RestResponse.status(Response.Status.NOT_FOUND, response);
    }

    @ServerExceptionMapper(priority = 3)
    public RestResponse<ExceptionResponseDTO> mapWebApplicationException(WebApplicationException ex) {
        if(ex.getCause() != null && ex.getCause() instanceof JsonParseException) {
            // JSON parsing exception
            final var response = new ExceptionResponseDTO(ex.getMessage(), Response.Status.BAD_REQUEST.getStatusCode(), LocalDateTime.now());
            logger.errorv(ex, "(mapJsonParseException) response: {0}", response);
            return RestResponse.status(Response.Status.BAD_REQUEST, response);
        }
        // re-throw all other types of error
        throw ex;
    }

    @ServerExceptionMapper(priority = 2)
    public RestResponse<ExceptionResponseDTO> mapInternalServerErrorException(InternalServerErrorException ex) {
        final var response = new ExceptionResponseDTO(ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), LocalDateTime.now());
        logger.errorv(ex, "(mapInternalServerErrorException) response: {0}", response);
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, response);
    }

    @ServerExceptionMapper(priority = 3)
    public RestResponse<ExceptionResponseDTO> mapRuntimeExceptionException(RuntimeException ex) {
        final var response = new ExceptionResponseDTO(ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), LocalDateTime.now());
        logger.errorv(ex, "(mapRuntimeExceptionException) response: {0}", response);
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, response);
    }
}
