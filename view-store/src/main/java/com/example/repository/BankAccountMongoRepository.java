package com.example.repository;

import com.example.domain.BankAccountDocument;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class BankAccountMongoRepository implements ReactivePanacheMongoRepository<BankAccountDocument> {

    public Uni<BankAccountDocument> findByAggregateId(String aggregateId) {
        return find("aggregateId", aggregateId).firstResult();
    }

    public Uni<List<BankAccountDocument>> findAllSortByBalanceWithPagination(Page page) {
        return findAll(Sort.ascending("balance")).page(page).list();
    }

}
