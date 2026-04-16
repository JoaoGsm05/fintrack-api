package com.fintrack.api.account.service;

import com.fintrack.api.account.dto.AccountMapper;
import com.fintrack.api.account.dto.AccountRequest;
import com.fintrack.api.account.dto.AccountResponse;
import com.fintrack.api.account.dto.AccountUpdateRequest;
import com.fintrack.api.account.entity.Account;
import com.fintrack.api.account.repository.AccountRepository;
import com.fintrack.api.shared.exception.BusinessRuleException;
import com.fintrack.api.shared.exception.ResourceNotFoundException;
import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.entity.TransactionType;
import com.fintrack.api.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse create(UUID userId, AccountRequest request) {
        BigDecimal initialBalance = request.balance() != null ? request.balance() : BigDecimal.ZERO;

        // 1. Cria a conta com saldo zero inicialmente
        Account account = Account.builder()
                .userId(userId)
                .name(request.name())
                .type(request.type())
                .balance(BigDecimal.ZERO)
                .currency(request.currency())
                .build();
        
        account = accountRepository.save(account);

        // 2. Se houver saldo inicial, cria uma transação de ajuste
        if (initialBalance.compareTo(BigDecimal.ZERO) != 0) {
            Transaction initialTransaction = Transaction.builder()
                    .userId(userId)
                    .accountId(account.getId())
                    .type(initialBalance.compareTo(BigDecimal.ZERO) > 0 ? TransactionType.INCOME : TransactionType.EXPENSE)
                    .amount(initialBalance.abs())
                    .description("Saldo inicial")
                    .date(LocalDate.now())
                    .build();
            
            transactionRepository.save(initialTransaction);
            
            // 3. Atualiza o saldo da conta baseado na transação
            account.setBalance(initialBalance);
            accountRepository.save(account);
        }

        return accountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listAll(UUID userId) {
        return accountMapper.toResponseList(accountRepository.findAllByUserIdAndDeletedAtIsNull(userId));
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(UUID userId, UUID id) {
        return accountMapper.toResponse(buscarContaDoUsuario(userId, id));
    }

    @Transactional
    public AccountResponse update(UUID userId, UUID id, AccountUpdateRequest request) {
        Account account = buscarContaDoUsuario(userId, id);
        if (request.name() != null)     account.setName(request.name());
        if (request.type() != null)     account.setType(request.type());
        if (request.currency() != null) account.setCurrency(request.currency());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Account account = buscarContaDoUsuario(userId, id);

        if (transactionRepository.existsByAccountIdAndDeletedAtIsNull(id)) {
            throw new BusinessRuleException(
                    "Não é possível excluir a conta pois ela possui transações ativas");
        }

        account.setDeletedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    // ── Método auxiliar ───────────────────────────────────────────────────────

    private Account buscarContaDoUsuario(UUID userId, UUID id) {
        return accountRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", id));
    }
}
