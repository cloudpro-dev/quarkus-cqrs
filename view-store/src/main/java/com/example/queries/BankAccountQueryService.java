package com.example.queries;

import com.example.domain.BankAccountDocument;
import com.example.dto.BankAccountResponseDTO;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface BankAccountQueryService {
    Uni<BankAccountResponseDTO> handle(GetBankAccountByIdQuery query);
    Uni<List<BankAccountDocument>> handle(FindAllByBalanceQuery query);
}