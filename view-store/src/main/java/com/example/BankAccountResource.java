package com.example;

import com.example.queries.BankAccountQueryService;
import com.example.queries.FindAllByBalanceQuery;
import com.example.queries.GetBankAccountByIdQuery;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

@Path("/api/v1/bank")
public class BankAccountResource {
    private final static Logger logger = Logger.getLogger(BankAccountResource.class);

    @Inject
    BankAccountQueryService queryService;

    @GET
    @Path("{aggregateId}")
    public Uni<Response> getBankAccount(@PathParam("aggregateId") String aggregateId) {
        final var query = new GetBankAccountByIdQuery(aggregateId);
        logger.infof("(HTTP getBankAccount) GetBankAccountByIDQuery: %s", query);
        return queryService.handle(query)
                .onItem().transform(aggregate -> Response.status(Response.Status.OK).entity(aggregate).build());
    }

    @GET
    @Path("/balance")
    public Uni<Response> getAllByBalance(@QueryParam("page") Optional<Integer> page, @QueryParam("size") Optional<Integer> size) {
        final var query = new FindAllByBalanceQuery(Page.of(page.orElse(0), size.orElse(5)));
        return queryService.handle(query).onItem().transform(result -> Response.status(Response.Status.OK).entity(result).build());
    }

}
