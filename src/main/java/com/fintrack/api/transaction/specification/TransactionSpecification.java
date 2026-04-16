package com.fintrack.api.transaction.specification;

import com.fintrack.api.transaction.dto.TransactionFilterRequest;
import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.entity.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> withFilters(UUID userId, TransactionFilterRequest filter) {
        return Specification
                .where(hasUserId(userId))
                .and(notDeleted())
                .and(onOrAfterDate(filter.startDate()))
                .and(onOrBeforeDate(filter.endDate()))
                .and(hasCategory(filter.categoryId()))
                .and(hasAccount(filter.accountId()))
                .and(hasType(filter.type()))
                .and(amountGreaterThanOrEqual(filter.minAmount()))
                .and(amountLessThanOrEqual(filter.maxAmount()))
                .and(descriptionContains(filter.description()));
    }

    private static Specification<Transaction> hasUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    private static Specification<Transaction> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    private static Specification<Transaction> onOrAfterDate(LocalDate startDate) {
        if (startDate == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate);
    }

    private static Specification<Transaction> onOrBeforeDate(LocalDate endDate) {
        if (endDate == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate);
    }

    private static Specification<Transaction> hasCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("categoryId"), categoryId);
    }

    private static Specification<Transaction> hasAccount(UUID accountId) {
        if (accountId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("accountId"), accountId);
    }

    private static Specification<Transaction> hasType(TransactionType type) {
        if (type == null) return null;
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    private static Specification<Transaction> amountGreaterThanOrEqual(BigDecimal min) {
        if (min == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    private static Specification<Transaction> amountLessThanOrEqual(BigDecimal max) {
        if (max == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    private static Specification<Transaction> descriptionContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        String normalizedKeyword = keyword.trim().toLowerCase();
        if (normalizedKeyword.length() < 3) {
            return null;
        }

        return (root, query, cb) ->
                cb.like(cb.lower(root.get("description")), normalizedKeyword + "%");
    }
}
