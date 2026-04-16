package com.fintrack.api.budget.repository;

import com.fintrack.api.budget.dto.BudgetSnapshot;
import com.fintrack.api.budget.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID>, JpaSpecificationExecutor<Budget> {

    Optional<Budget> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<Budget> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<Budget> findAllByUserIdAndCategoryIdAndDeletedAtIsNull(UUID userId, UUID categoryId);

    @Query("""
            SELECT new com.fintrack.api.budget.dto.BudgetSnapshot(
                b.id,
                b.userId,
                b.categoryId,
                COALESCE(c.name, 'N/A'),
                b.amount,
                b.period,
                b.startDate,
                b.endDate,
                COALESCE(SUM(t.amount), 0)
            )
            FROM Budget b
            LEFT JOIN Category c
                ON c.id = b.categoryId
               AND c.userId = b.userId
               AND c.deletedAt IS NULL
            LEFT JOIN Transaction t
                ON t.userId = b.userId
               AND t.categoryId = b.categoryId
               AND t.deletedAt IS NULL
               AND t.type = com.fintrack.api.transaction.entity.TransactionType.EXPENSE
               AND t.date BETWEEN b.startDate AND b.endDate
            WHERE b.userId = :userId
              AND b.deletedAt IS NULL
            GROUP BY
                b.id,
                b.userId,
                b.categoryId,
                c.name,
                b.amount,
                b.period,
                b.startDate,
                b.endDate
            ORDER BY b.startDate DESC, b.id DESC
            """)
    List<BudgetSnapshot> findBudgetSnapshotsByUserId(@Param("userId") UUID userId);

    @Query(
            value = """
                    SELECT new com.fintrack.api.budget.dto.BudgetSnapshot(
                        b.id,
                        b.userId,
                        b.categoryId,
                        COALESCE(c.name, 'N/A'),
                        b.amount,
                        b.period,
                        b.startDate,
                        b.endDate,
                        COALESCE(SUM(t.amount), 0)
                    )
                    FROM Budget b
                    LEFT JOIN Category c
                        ON c.id = b.categoryId
                       AND c.userId = b.userId
                       AND c.deletedAt IS NULL
                    LEFT JOIN Transaction t
                        ON t.userId = b.userId
                       AND t.categoryId = b.categoryId
                       AND t.deletedAt IS NULL
                       AND t.type = com.fintrack.api.transaction.entity.TransactionType.EXPENSE
                       AND t.date BETWEEN b.startDate AND b.endDate
                    WHERE b.userId = :userId
                      AND b.deletedAt IS NULL
                    GROUP BY
                        b.id,
                        b.userId,
                        b.categoryId,
                        c.name,
                        b.amount,
                        b.period,
                        b.startDate,
                        b.endDate
                    """,
            countQuery = """
                    SELECT COUNT(b)
                    FROM Budget b
                    WHERE b.userId = :userId
                      AND b.deletedAt IS NULL
                    """
    )
    Page<BudgetSnapshot> findBudgetSnapshotsByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT new com.fintrack.api.budget.dto.BudgetSnapshot(
                b.id,
                b.userId,
                b.categoryId,
                COALESCE(c.name, 'N/A'),
                b.amount,
                b.period,
                b.startDate,
                b.endDate,
                COALESCE(SUM(t.amount), 0)
            )
            FROM Budget b
            LEFT JOIN Category c
                ON c.id = b.categoryId
               AND c.userId = b.userId
               AND c.deletedAt IS NULL
            LEFT JOIN Transaction t
                ON t.userId = b.userId
               AND t.categoryId = b.categoryId
               AND t.deletedAt IS NULL
               AND t.type = com.fintrack.api.transaction.entity.TransactionType.EXPENSE
               AND t.date BETWEEN b.startDate AND b.endDate
            WHERE b.id = :id
              AND b.userId = :userId
              AND b.deletedAt IS NULL
            GROUP BY
                b.id,
                b.userId,
                b.categoryId,
                c.name,
                b.amount,
                b.period,
                b.startDate,
                b.endDate
            """)
    Optional<BudgetSnapshot> findBudgetSnapshotByIdAndUserId(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );

    @Query("""
            SELECT new com.fintrack.api.budget.dto.BudgetSnapshot(
                b.id,
                b.userId,
                b.categoryId,
                COALESCE(c.name, 'N/A'),
                b.amount,
                b.period,
                b.startDate,
                b.endDate,
                COALESCE(SUM(t.amount), 0)
            )
            FROM Budget b
            LEFT JOIN Category c
                ON c.id = b.categoryId
               AND c.userId = b.userId
               AND c.deletedAt IS NULL
            LEFT JOIN Transaction t
                ON t.userId = b.userId
               AND t.categoryId = b.categoryId
               AND t.deletedAt IS NULL
               AND t.type = com.fintrack.api.transaction.entity.TransactionType.EXPENSE
               AND t.date BETWEEN b.startDate AND b.endDate
            WHERE b.userId = :userId
              AND b.categoryId = :categoryId
              AND b.deletedAt IS NULL
            GROUP BY
                b.id,
                b.userId,
                b.categoryId,
                c.name,
                b.amount,
                b.period,
                b.startDate,
                b.endDate
            ORDER BY b.startDate DESC, b.id DESC
            """)
    List<BudgetSnapshot> findBudgetSnapshotsByUserIdAndCategoryId(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId
    );

    @Query("""
            SELECT b FROM Budget b
            WHERE b.userId = :userId
              AND b.deletedAt IS NULL
              AND b.startDate <= :endDate
              AND b.endDate >= :startDate
            """)
    List<Budget> findBudgetsIntersectingDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Budget b
            SET b.alert80SentAt = :sentAt
            WHERE b.id = :budgetId
              AND b.userId = :userId
              AND b.deletedAt IS NULL
              AND b.alert80SentAt IS NULL
            """)
    int markAlert80AsSent(
            @Param("budgetId") UUID budgetId,
            @Param("userId") UUID userId,
            @Param("sentAt") LocalDateTime sentAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Budget b
            SET b.alert100SentAt = :sentAt
            WHERE b.id = :budgetId
              AND b.userId = :userId
              AND b.deletedAt IS NULL
              AND b.alert100SentAt IS NULL
            """)
    int markAlert100AsSent(
            @Param("budgetId") UUID budgetId,
            @Param("userId") UUID userId,
            @Param("sentAt") LocalDateTime sentAt
    );

    Optional<Budget> findByUserIdAndCategoryIdAndDeletedAtIsNull(UUID userId, UUID categoryId);
}
