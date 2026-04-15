package com.fintrack.api.transaction.service;

import com.fintrack.api.account.entity.Account;
import com.fintrack.api.account.entity.AccountType;
import com.fintrack.api.account.repository.AccountRepository;
import com.fintrack.api.shared.exception.BusinessRuleException;
import com.fintrack.api.shared.exception.ResourceNotFoundException;
import com.fintrack.api.transaction.dto.TransactionFilterRequest;
import com.fintrack.api.transaction.dto.TransactionMapper;
import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.dto.TransactionUpdateRequest;
import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.entity.TransactionType;
import com.fintrack.api.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

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
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private UUID userId;
    private UUID accountId;
    private UUID transactionId;
    private Account fakeAccount;
    private Transaction fakeTransaction;
    private TransactionResponse fakeResponse;

    @BeforeEach
    void setUp() {
        userId        = UUID.randomUUID();
        accountId     = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        fakeAccount = Account.builder()
                .userId(userId)
                .name("Nubank")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .currency("BRL")
                .build();

        fakeTransaction = Transaction.builder()
                .userId(userId)
                .accountId(accountId)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .build();

        fakeResponse = new TransactionResponse(transactionId, userId, accountId, null,
                TransactionType.INCOME, new BigDecimal("100.00"), null, LocalDate.now(), null, null);
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Salva transação e recalcula saldo da conta")
        void create_validRequest_savesAndRecalculatesBalance() {
            TransactionRequest request = new TransactionRequest(
                    accountId, null, TransactionType.INCOME, new BigDecimal("100.00"), null, LocalDate.now());

            when(accountRepository.existsByIdAndUserIdAndDeletedAtIsNull(accountId, userId)).thenReturn(true);
            when(transactionRepository.save(any())).thenReturn(fakeTransaction);
            when(transactionMapper.toResponse(fakeTransaction)).thenReturn(fakeResponse);
            when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                    .thenReturn(Optional.of(fakeAccount));
            when(transactionRepository.calculateBalanceByAccountId(accountId))
                    .thenReturn(new BigDecimal("100.00"));

            TransactionResponse result = transactionService.create(userId, request);

            assertThat(result).isEqualTo(fakeResponse);
            verify(transactionRepository).save(any(Transaction.class));
            verify(accountRepository).save(fakeAccount);
            assertThat(fakeAccount.getBalance()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("Lança BusinessRuleException quando conta não pertence ao usuário")
        void create_accountNotOwned_throwsBusinessRuleException() {
            TransactionRequest request = new TransactionRequest(
                    accountId, null, TransactionType.INCOME, new BigDecimal("50.00"), null, LocalDate.now());
            when(accountRepository.existsByIdAndUserIdAndDeletedAtIsNull(accountId, userId)).thenReturn(false);

            assertThatThrownBy(() -> transactionService.create(userId, request))
                    .isInstanceOf(BusinessRuleException.class);
            verify(transactionRepository, never()).save(any());
        }
    }

    // ── listAll() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listAll()")
    class ListAll {

        @Test
        @DisplayName("Retorna página de transações filtradas")
        void listAll_returnsFilteredPage() {
            TransactionFilterRequest filter = new TransactionFilterRequest(
                    null, null, null, null, null, null, null, null);
            PageRequest pageable = PageRequest.of(0, 20);
            Page<Transaction> page = new PageImpl<>(List.of(fakeTransaction));

            when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(transactionMapper.toResponse(fakeTransaction)).thenReturn(fakeResponse);

            Page<TransactionResponse> result = transactionService.listAll(userId, filter, pageable);

            assertThat(result.getContent()).hasSize(1).contains(fakeResponse);
        }
    }

    // ── findById() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Retorna transação quando encontrada")
        void findById_found_returnsResponse() {
            when(transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(transactionId, userId))
                    .thenReturn(Optional.of(fakeTransaction));
            when(transactionMapper.toResponse(fakeTransaction)).thenReturn(fakeResponse);

            assertThat(transactionService.findById(userId, transactionId)).isEqualTo(fakeResponse);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando não encontrada")
        void findById_notFound_throwsResourceNotFoundException() {
            when(transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(transactionId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.findById(userId, transactionId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Aplica soft delete e recalcula saldo da conta")
        void delete_found_softDeletesAndRecalculatesBalance() {
            when(transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(transactionId, userId))
                    .thenReturn(Optional.of(fakeTransaction));
            when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                    .thenReturn(Optional.of(fakeAccount));
            when(transactionRepository.calculateBalanceByAccountId(accountId))
                    .thenReturn(BigDecimal.ZERO);

            transactionService.delete(userId, transactionId);

            assertThat(fakeTransaction.getDeletedAt()).isNotNull();
            verify(transactionRepository).save(fakeTransaction);
            verify(accountRepository).save(fakeAccount);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando não encontrada")
        void delete_notFound_throwsResourceNotFoundException() {
            when(transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(transactionId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.delete(userId, transactionId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
