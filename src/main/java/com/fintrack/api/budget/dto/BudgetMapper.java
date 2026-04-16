package com.fintrack.api.budget.dto;

import com.fintrack.api.budget.entity.Budget;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BudgetMapper {

    public BudgetResponse toResponse(Budget budget, String categoryName,
                                     BigDecimal spentAmount, BigDecimal remainingAmount) {
        return new BudgetResponse(
                budget.getId(),
                budget.getUserId(),
                budget.getCategoryId(),
                categoryName,
                budget.getAmount(),
                budget.getPeriod(),
                budget.getStartDate(),
                budget.getEndDate(),
                spentAmount,
                remainingAmount
        );
    }

    public BudgetResponse toResponse(BudgetSnapshot snapshot) {
        BigDecimal spentAmount = snapshot.spentAmount() != null ? snapshot.spentAmount() : BigDecimal.ZERO;
        return new BudgetResponse(
                snapshot.id(),
                snapshot.userId(),
                snapshot.categoryId(),
                snapshot.categoryName(),
                snapshot.amount(),
                snapshot.period(),
                snapshot.startDate(),
                snapshot.endDate(),
                spentAmount,
                snapshot.amount().subtract(spentAmount)
        );
    }
}
