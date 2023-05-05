package com.example;

import com.example.exception.ExceptionResponseDTO;
import com.example.exception.InsufficientFundsException;
import com.example.store.exception.BankAccountNotFoundException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;

@ApplicationScoped
public class BankAccountExceptionMapper {

    private final static Logger logger = Logger.getLogger(BankAccountExceptionMapper.class);

    @ServerExceptionMapper(priority = 1)
    public RestResponse<ExceptionResponseDTO> mapInsufficientFundsException(InsufficientFundsException ex) {
        final var response = new ExceptionResponseDTO(ex.getMessage(), Response.Status.BAD_REQUEST.getStatusCode(), LocalDateTime.now());
        logger.error("(mapInsufficientFundsException) response: %s", response, ex);
        return RestResponse.status(Response.Status.BAD_REQUEST, response);
    }

    @ServerExceptionMapper(priority = 2)
    public RestResponse<ExceptionResponseDTO> mapBankAccountNotFoundException(BankAccountNotFoundException ex) {
        final var response = new ExceptionResponseDTO(ex.getMessage(), Response.Status.NOT_FOUND.getStatusCode(), LocalDateTime.now());
        logger.error("(mapBankAccountNotFoundException) response: %s", response, ex);
        return RestResponse.status(Response.Status.NOT_FOUND, response);
    }

    @ServerExceptionMapper(priority = 3)
    public RestResponse<ExceptionResponseDTO> mapInternalServerErrorException(InternalServerErrorException ex) {
        final var response = new ExceptionResponseDTO(ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), LocalDateTime.now());
        logger.error("(mapInternalServerErrorException) response: %s", response, ex);
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, response);
    }

    @ServerExceptionMapper(priority = 5)
    public RestResponse<ExceptionResponseDTO> mapRuntimeExceptionException(RuntimeException ex) {
        final var response = new ExceptionResponseDTO(ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), LocalDateTime.now());
        logger.error("(mapRuntimeExceptionException) response: %s", response, ex);
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, response);
    }


}
