package com.example.queries;

import com.example.exception.BankAccountNotFoundException;
import com.example.util.BankAccountMapper;
import com.example.domain.BankAccountDocument;
import com.example.repository.BankAccountMongoRepository;
import com.example.dto.BankAccountResponseDTO;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class BankAccountQueryHandler implements BankAccountQueryService {

    private final static Logger logger = Logger.getLogger(BankAccountQueryHandler.class);

    @Inject
    BankAccountMongoRepository panacheRepository;

    @Override
    public Uni<BankAccountResponseDTO> handle(GetBankAccountByIdQuery query) {
        return panacheRepository.findByAggregateId(query.aggregateId())
                .onItem().ifNull().failWith(BankAccountNotFoundException::new)
                .onItem().transform(BankAccountMapper::bankAccountResponseDTOFromDocument)
                .onItem().invoke(bankAccountResponseDTO -> logger.infof("(FIND panacheRepository.findByAggregateId) bankAccountResponseDTO: %s", bankAccountResponseDTO))
                .onFailure().invoke(ex -> logger.errorf("mongo aggregate not found: %s", ex.getMessage()));
    }

    @Override
    public Uni<List<BankAccountDocument>> handle(FindAllByBalanceQuery query) {
        return panacheRepository.findAllSortByBalanceWithPagination(query.page())
                .onItem().invoke(result -> logger.infof("(findAllSortByBalanceWithPagination) query: %s", query));
    }
}
