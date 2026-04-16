package com.fintrack.api.recurring.repository;

import com.fintrack.api.recurring.entity.RecurringTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {

    List<RecurringTransaction> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<RecurringTransaction> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<RecurringTransaction> findAllByActiveTrueAndDeletedAtIsNullAndNextOccurrenceLessThanEqual(LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT rt
            FROM RecurringTransaction rt
            WHERE rt.active = true
              AND rt.deletedAt IS NULL
              AND rt.nextOccurrence <= :date
            """)
    List<RecurringTransaction> findDueTransactionsForUpdate(LocalDate date);
}
