package com.example;

import com.example.queries.BankAccountQueryService;
import com.example.queries.FindAllByBalanceQuery;
import com.example.queries.GetBankAccountByIdQuery;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
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
