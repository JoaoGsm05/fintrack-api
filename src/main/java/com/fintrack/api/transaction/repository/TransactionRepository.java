package com.fintrack.api.transaction.repository;

import com.fintrack.api.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    /** Verifica se a conta tem transações ativas — usado ao deletar Account. */
    boolean existsByAccountIdAndDeletedAtIsNull(UUID accountId);

    /** Verifica se a categoria tem transações ativas — usado ao deletar Category. */
    boolean existsByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    /** Recalcula o saldo da conta somando/subtraindo transações ativas. */
    @Query("""
            SELECT COALESCE(
                SUM(CASE WHEN t.type = 'INCOME'  THEN t.amount ELSE -t.amount END),
                0
            )
            FROM Transaction t
            WHERE t.accountId = :accountId
              AND t.deletedAt IS NULL
            """)
    java.math.BigDecimal calculateBalanceByAccountId(@Param("accountId") UUID accountId);
}
