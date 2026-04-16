package com.fintrack.api.transaction.repository;

import com.fintrack.api.report.dto.CategoryExpenseAggregate;
import com.fintrack.api.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    boolean existsByAccountIdAndDeletedAtIsNull(UUID accountId);

    boolean existsByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Transaction t
            SET t.categoryId = null
            WHERE t.userId = :userId
              AND t.categoryId = :categoryId
              AND t.deletedAt IS NULL
            """)
    int clearCategoryReferences(@Param("userId") UUID userId, @Param("categoryId") UUID categoryId);

    @Query("""
            SELECT COALESCE(
                SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END),
                0
            )
            FROM Transaction t
            WHERE t.accountId = :accountId
              AND t.deletedAt IS NULL
            """)
    java.math.BigDecimal calculateBalanceByAccountId(@Param("accountId") UUID accountId);

    @Query("""
            SELECT new com.fintrack.api.report.dto.CategoryExpenseAggregate(
                COALESCE(c.name, 'Outros'),
                COALESCE(SUM(t.amount), 0)
            )
            FROM Transaction t
            LEFT JOIN Category c
                ON c.id = t.categoryId
               AND c.userId = :userId
               AND c.deletedAt IS NULL
            WHERE t.userId = :userId
              AND t.type = com.fintrack.api.transaction.entity.TransactionType.EXPENSE
              AND t.date BETWEEN :startDate AND :endDate
              AND t.deletedAt IS NULL
            GROUP BY COALESCE(c.name, 'Outros')
            ORDER BY COALESCE(SUM(t.amount), 0) DESC, COALESCE(c.name, 'Outros') ASC
            """)
    java.util.List<CategoryExpenseAggregate> sumExpensesByCategory(
            @Param("userId") UUID userId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate
    );
}
