package com.example;

import com.example.commands.*;
import com.example.dto.*;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/bank")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BankAccountResource {

    private final static Logger logger = Logger.getLogger(BankAccountResource.class);

    @Inject
    BankAccountCommandService commandService;

    @POST
    public Uni<Response> createBankAccount(@Valid CreateBankAccountRequestDTO dto) {
        final var command = new CreateBankAccountCommand(dto.email(), dto.userName(), dto.address());
        logger.infof("CreateBankAccountCommand: %s", command);
        return commandService.handle(command)
                .onItem().transform(id -> Response.status(Response.Status.CREATED).entity(id).build());
    }

    @POST
    @Path("/email/{aggregateID}")
    public Uni<Response> changeEmail(@PathParam("aggregateID") String aggregateID, @Valid ChangeEmailRequestDTO dto) {
        final var command = new ChangeEmailCommand(aggregateID, dto.email());
        logger.infof("ChangeEmailCommand: %s", command);
        return commandService.handle(command)
                .onItem().transform(v -> Response.status(Response.Status.NO_CONTENT).build());
    }

    @POST
    @Path("/address/{aggregateID}")
    public Uni<Response> changeAddress(@PathParam("aggregateID") String aggregateID, @Valid ChangeAddressRequestDTO dto) {
        final var command = new ChangeAddressCommand(aggregateID, dto.address());
        logger.infof("ChangeAddressCommand: %s", command);
        return commandService.handle(command)
                .onItem().transform(v -> Response.status(Response.Status.NO_CONTENT).build());
    }

    @POST
    @Path("/deposit/{aggregateID}")
    public Uni<Response> depositAmount(@PathParam("aggregateID") String aggregateID, @Valid DepositAmountRequestDTO dto) {
        final var command = new DepositAmountCommand(aggregateID, dto.amount());
        logger.infof("DepositAmountCommand: %s", command);
        return commandService.handle(command)
                .onItem().transform(v -> Response.status(Response.Status.NO_CONTENT).build());
    }

    @POST
    @Path("/withdraw/{aggregateID}")
    public Uni<Response> withdrawAmount(@PathParam("aggregateID") String aggregateID, @Valid WithdrawAmountRequestDTO dto) {
        final var command = new WithdrawAmountCommand(aggregateID, dto.amount());
        logger.infof("WithdrawAmountCommand: %s", command);
        return commandService.handle(command)
                .onItem().transform(v -> Response.status(Response.Status.NO_CONTENT).build());
    }

}
