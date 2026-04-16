package com.fintrack.api.recurring.dto;

import com.fintrack.api.recurring.entity.RecurringTransaction;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionMapper {

    public RecurringTransactionResponse toResponse(RecurringTransaction entity) {
        return new RecurringTransactionResponse(
                entity.getId(),
                entity.getAccountId(),
                entity.getCategoryId(),
                entity.getType(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getFrequency(),
                entity.getNextOccurrence(),
                entity.isActive()
        );
    }

    public RecurringTransaction toEntity(RecurringTransactionRequest request) {
        return RecurringTransaction.builder()
                .accountId(request.accountId())
                .categoryId(request.categoryId())
                .type(request.type())
                .amount(request.amount())
                .description(request.description())
                .frequency(request.frequency())
                .nextOccurrence(request.nextOccurrence())
                .active(true)
                .build();
    }
}
