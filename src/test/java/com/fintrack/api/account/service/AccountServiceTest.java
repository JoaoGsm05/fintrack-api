package com.fintrack.api.account.service;

import com.fintrack.api.account.dto.AccountMapper;
import com.fintrack.api.account.dto.AccountRequest;
import com.fintrack.api.account.dto.AccountResponse;
import com.fintrack.api.account.dto.AccountUpdateRequest;
import com.fintrack.api.account.entity.Account;
import com.fintrack.api.account.entity.AccountType;
import com.fintrack.api.account.repository.AccountRepository;
import com.fintrack.api.shared.exception.BusinessRuleException;
import com.fintrack.api.shared.exception.ResourceNotFoundException;
import com.fintrack.api.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private UUID userId;
    private UUID accountId;
    private Account fakeAccount;
    private AccountResponse fakeResponse;

    @BeforeEach
    void setUp() {
        userId    = UUID.randomUUID();
        accountId = UUID.randomUUID();
        fakeAccount = Account.builder()
                .userId(userId)
                .name("Nubank")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .currency("BRL")
                .build();
        fakeResponse = new AccountResponse(accountId, userId, "Nubank",
                AccountType.CHECKING, BigDecimal.ZERO, "BRL", null, null);
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Salva conta e retorna AccountResponse")
        void create_validRequest_savesAndReturnsResponse() {
            AccountRequest request = new AccountRequest("Nubank", AccountType.CHECKING, BigDecimal.ZERO, "BRL");
            when(accountRepository.save(any())).thenReturn(fakeAccount);
            when(accountMapper.toResponse(fakeAccount)).thenReturn(fakeResponse);

            AccountResponse result = accountService.create(userId, request);

            assertThat(result).isEqualTo(fakeResponse);
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("Usa saldo zero quando balance é null")
        void create_nullBalance_usesZero() {
            AccountRequest request = new AccountRequest("Nubank", AccountType.CHECKING, null, "BRL");
            when(accountRepository.save(any())).thenAnswer(inv -> {
                Account a = inv.getArgument(0);
                assertThat(a.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
                return a;
            });
            when(accountMapper.toResponse(any())).thenReturn(fakeResponse);

            accountService.create(userId, request);

            verify(accountRepository).save(any(Account.class));
        }
    }

    // ── listAll() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listAll()")
    class ListAll {

        @Test
        @DisplayName("Retorna lista de contas do usuário")
        void listAll_returnsUserAccounts() {
            when(accountRepository.findAllByUserIdAndDeletedAtIsNull(userId))
                    .thenReturn(List.of(fakeAccount));
            when(accountMapper.toResponseList(List.of(fakeAccount)))
                    .thenReturn(List.of(fakeResponse));

            List<AccountResponse> result = accountService.listAll(userId);

            assertThat(result).hasSize(1).contains(fakeResponse);
        }
    }

    // ── findById() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Retorna conta quando encontrada")
        void findById_found_returnsResponse() {
            when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                    .thenReturn(Optional.of(fakeAccount));
            when(accountMapper.toResponse(fakeAccount)).thenReturn(fakeResponse);

            AccountResponse result = accountService.findById(userId, accountId);

            assertThat(result).isEqualTo(fakeResponse);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando não encontrada")
        void findById_notFound_throwsResourceNotFoundException() {
            when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.findById(userId, accountId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── update() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Atualiza campos informados e retorna response")
        void update_validRequest_updatesAndReturnsResponse() {
            AccountUpdateRequest request = new AccountUpdateRequest("Conta Atualizada", AccountType.SAVINGS, "USD");
            when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                    .thenReturn(Optional.of(fakeAccount));
            when(accountRepository.save(fakeAccount)).thenReturn(fakeAccount);
            when(accountMapper.toResponse(fakeAccount)).thenReturn(fakeResponse);

            accountService.update(userId, accountId, request);

            assertThat(fakeAccount.getName()).isEqualTo("Conta Atualizada");
            assertThat(fakeAccount.getType()).isEqualTo(AccountType.SAVINGS);
            assertThat(fakeAccount.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando conta não pertence ao usuário")
        void update_notFound_throwsResourceNotFoundException() {
            when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.update(userId, accountId,
                    new AccountUpdateRequest("X", AccountType.CHECKING, "BRL")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Aplica soft delete quando conta não tem transações ativas")
        void delete_noActiveTransactions_softDeletes() {
            when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                    .thenReturn(Optional.of(fakeAccount));
            when(transactionRepository.existsByAccountIdAndDeletedAtIsNull(accountId))
                    .thenReturn(false);

            accountService.delete(userId, accountId);

            assertThat(fakeAccount.getDeletedAt()).isNotNull();
            verify(accountRepository).save(fakeAccount);
        }

        @Test
        @DisplayName("Lança BusinessRuleException quando conta tem transações ativas")
        void delete_hasActiveTransactions_throwsBusinessRuleException() {
            when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                    .thenReturn(Optional.of(fakeAccount));
            when(transactionRepository.existsByAccountIdAndDeletedAtIsNull(accountId))
                    .thenReturn(true);

            assertThatThrownBy(() -> accountService.delete(userId, accountId))
                    .isInstanceOf(BusinessRuleException.class);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando conta não encontrada")
        void delete_notFound_throwsResourceNotFoundException() {
            when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.delete(userId, accountId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
