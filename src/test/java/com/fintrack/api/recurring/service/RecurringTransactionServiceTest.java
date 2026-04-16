package com.fintrack.api.recurring.service;

import com.fintrack.api.account.repository.AccountRepository;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.recurring.dto.RecurringTransactionMapper;
import com.fintrack.api.recurring.dto.RecurringTransactionRequest;
import com.fintrack.api.recurring.dto.RecurringTransactionResponse;
import com.fintrack.api.recurring.entity.RecurringFrequency;
import com.fintrack.api.recurring.entity.RecurringTransaction;
import com.fintrack.api.recurring.repository.RecurringTransactionRepository;
import com.fintrack.api.shared.exception.BusinessRuleException;
import com.fintrack.api.shared.exception.ResourceNotFoundException;
import com.fintrack.api.transaction.entity.TransactionType;
import com.fintrack.api.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock private RecurringTransactionRepository recurringTransactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private RecurringTransactionMapper recurringTransactionMapper;
    @Mock private TransactionService transactionService;

    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    private UUID userId;
    private UUID recurringId;
    private UUID accountId;
    private UUID categoryId;
    private RecurringTransaction fakeEntity;
    private RecurringTransactionResponse fakeResponse;

    @BeforeEach
    void setUp() {
        userId      = UUID.randomUUID();
        recurringId = UUID.randomUUID();
        accountId   = UUID.randomUUID();
        categoryId  = UUID.randomUUID();

        fakeEntity = RecurringTransaction.builder()
                .userId(userId)
                .accountId(accountId)
                .categoryId(categoryId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("200.00"))
                .description("Netflix")
                .frequency(RecurringFrequency.MONTHLY)
                .nextOccurrence(LocalDate.of(2026, 5, 1))
                .active(true)
                .build();

        fakeResponse = new RecurringTransactionResponse(
                recurringId, accountId, categoryId,
                TransactionType.EXPENSE, new BigDecimal("200.00"),
                "Netflix", RecurringFrequency.MONTHLY,
                LocalDate.of(2026, 5, 1), true
        );
    }

    private RecurringTransactionRequest buildRequest() {
        return new RecurringTransactionRequest(
                accountId, categoryId, TransactionType.EXPENSE,
                new BigDecimal("200.00"), "Netflix",
                RecurringFrequency.MONTHLY, LocalDate.of(2026, 5, 1)
        );
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Salva transação recorrente e retorna response")
        void create_valid_savesAndReturns() {
            when(accountRepository.existsByIdAndUserIdAndDeletedAtIsNull(accountId, userId)).thenReturn(true);
            when(categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)).thenReturn(true);
            when(recurringTransactionMapper.toEntity(any())).thenReturn(fakeEntity);
            when(recurringTransactionRepository.save(any())).thenReturn(fakeEntity);
            when(recurringTransactionMapper.toResponse(fakeEntity)).thenReturn(fakeResponse);

            RecurringTransactionResponse result = recurringTransactionService.create(userId, buildRequest());

            assertThat(result).isEqualTo(fakeResponse);
            verify(recurringTransactionRepository).save(fakeEntity);
        }

        @Test
        @DisplayName("Lança BusinessRuleException quando conta não pertence ao usuário")
        void create_invalidAccount_throwsBusinessRuleException() {
            when(accountRepository.existsByIdAndUserIdAndDeletedAtIsNull(accountId, userId)).thenReturn(false);

            assertThatThrownBy(() -> recurringTransactionService.create(userId, buildRequest()))
                    .isInstanceOf(BusinessRuleException.class);
            verify(recurringTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança BusinessRuleException quando categoria não pertence ao usuário")
        void create_invalidCategory_throwsBusinessRuleException() {
            when(accountRepository.existsByIdAndUserIdAndDeletedAtIsNull(accountId, userId)).thenReturn(true);
            when(categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)).thenReturn(false);

            assertThatThrownBy(() -> recurringTransactionService.create(userId, buildRequest()))
                    .isInstanceOf(BusinessRuleException.class);
            verify(recurringTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Não valida categoria quando categoryId é null")
        void create_nullCategory_skipsCategoryValidation() {
            RecurringTransactionRequest requestSemCategoria = new RecurringTransactionRequest(
                    accountId, null, TransactionType.EXPENSE,
                    new BigDecimal("200.00"), "Sem categoria",
                    RecurringFrequency.MONTHLY, LocalDate.of(2026, 5, 1)
            );
            when(accountRepository.existsByIdAndUserIdAndDeletedAtIsNull(accountId, userId)).thenReturn(true);
            when(recurringTransactionMapper.toEntity(any())).thenReturn(fakeEntity);
            when(recurringTransactionRepository.save(any())).thenReturn(fakeEntity);
            when(recurringTransactionMapper.toResponse(fakeEntity)).thenReturn(fakeResponse);

            recurringTransactionService.create(userId, requestSemCategoria);

            verify(categoryRepository, never()).existsByIdAndUserIdAndDeletedAtIsNull(any(), any());
        }
    }

    // ── listAll() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listAll()")
    class ListAll {

        @Test
        @DisplayName("Retorna lista de transações recorrentes do usuário")
        void listAll_returnsUserTransactions() {
            when(recurringTransactionRepository.findAllByUserIdAndDeletedAtIsNull(userId))
                    .thenReturn(List.of(fakeEntity));
            when(recurringTransactionMapper.toResponse(fakeEntity)).thenReturn(fakeResponse);

            List<RecurringTransactionResponse> result = recurringTransactionService.listAll(userId);

            assertThat(result).hasSize(1).contains(fakeResponse);
        }
    }

    // ── findById() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Retorna response quando encontrada")
        void findById_found_returnsResponse() {
            when(recurringTransactionRepository.findByIdAndUserIdAndDeletedAtIsNull(recurringId, userId))
                    .thenReturn(Optional.of(fakeEntity));
            when(recurringTransactionMapper.toResponse(fakeEntity)).thenReturn(fakeResponse);

            assertThat(recurringTransactionService.findById(userId, recurringId)).isEqualTo(fakeResponse);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando não encontrada")
        void findById_notFound_throwsResourceNotFoundException() {
            when(recurringTransactionRepository.findByIdAndUserIdAndDeletedAtIsNull(recurringId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> recurringTransactionService.findById(userId, recurringId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Aplica soft delete")
        void delete_found_softDeletes() {
            when(recurringTransactionRepository.findByIdAndUserIdAndDeletedAtIsNull(recurringId, userId))
                    .thenReturn(Optional.of(fakeEntity));

            recurringTransactionService.delete(userId, recurringId);

            assertThat(fakeEntity.getDeletedAt()).isNotNull();
            verify(recurringTransactionRepository).save(fakeEntity);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando não encontrada")
        void delete_notFound_throwsResourceNotFoundException() {
            when(recurringTransactionRepository.findByIdAndUserIdAndDeletedAtIsNull(recurringId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> recurringTransactionService.delete(userId, recurringId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(recurringTransactionRepository, never()).save(any());
        }
    }

    // ── processRecurringTransactions() ───────────────────────────────────────

    @Nested
    @DisplayName("processRecurringTransactions()")
    class ProcessRecurring {

        @Test
        @DisplayName("Processa transações vencidas e avança a próxima ocorrência")
        void process_dueTransactions_createsAndAdvances() {
            LocalDate today = LocalDate.now();
            fakeEntity.setNextOccurrence(today.minusDays(1)); // vencida ontem

            when(recurringTransactionRepository
                    .findDueTransactionsForUpdate(today))
                    .thenReturn(List.of(fakeEntity));
            when(recurringTransactionRepository.save(any())).thenReturn(fakeEntity);

            recurringTransactionService.processRecurringTransactions();

            verify(transactionService).create(eq(userId), any());
            // Data deve ter avançado um mês (MONTHLY)
            assertThat(fakeEntity.getNextOccurrence()).isAfter(today.minusDays(1));
        }

        @Test
        @DisplayName("Não processa nada quando não há transações vencidas")
        void process_noDueTransactions_doesNothing() {
            when(recurringTransactionRepository
                    .findDueTransactionsForUpdate(any()))
                    .thenReturn(List.of());

            recurringTransactionService.processRecurringTransactions();

            verify(transactionService, never()).create(any(), any());
        }
    }
}
