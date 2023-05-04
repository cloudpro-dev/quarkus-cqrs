package com.example.util;

import com.example.domain.BankAccountDocument;
import com.example.dto.BankAccountResponseDTO;

public final class BankAccountMapper {

    private BankAccountMapper() {
    }


    public static BankAccountResponseDTO bankAccountResponseDTOFromDocument(BankAccountDocument bankAccountDocument) {
        return new BankAccountResponseDTO(
                bankAccountDocument.getAggregateId(),
                bankAccountDocument.getEmail(),
                bankAccountDocument.getAddress(),
                bankAccountDocument.getUserName(),
                bankAccountDocument.getBalance()
        );
    }

}
