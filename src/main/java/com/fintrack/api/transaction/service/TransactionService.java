package com.fintrack.api.transaction.service;

import com.fintrack.api.account.repository.AccountRepository;
import com.fintrack.api.shared.exception.BusinessRuleException;
import com.fintrack.api.shared.exception.ResourceNotFoundException;
import com.fintrack.api.transaction.dto.TransactionFilterRequest;
import com.fintrack.api.transaction.dto.TransactionMapper;
import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.dto.TransactionUpdateRequest;
import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.repository.TransactionRepository;
import com.fintrack.api.transaction.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse create(UUID userId, TransactionRequest request) {
        validarContaDoUsuario(userId, request.accountId());

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .accountId(request.accountId())
                .categoryId(request.categoryId())
                .type(request.type())
                .amount(request.amount())
                .description(request.description())
                .date(request.date())
                .build();

        Transaction salva = transactionRepository.save(transaction);
        recalcularSaldo(userId, request.accountId());
        return transactionMapper.toResponse(salva);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> listAll(UUID userId, TransactionFilterRequest filter, Pageable pageable) {
        Specification<Transaction> spec = TransactionSpecification.withFilters(userId, filter);
        return transactionRepository.findAll(spec, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(UUID userId, UUID id) {
        return transactionMapper.toResponse(buscarTransacaoDoUsuario(userId, id));
    }

    @Transactional
    public TransactionResponse update(UUID userId, UUID id, TransactionUpdateRequest request) {
        Transaction transaction = buscarTransacaoDoUsuario(userId, id);
        UUID contaAntiga = transaction.getAccountId();

        validarContaDoUsuario(userId, request.accountId());

        transaction.setAccountId(request.accountId());
        transaction.setCategoryId(request.categoryId());
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setDate(request.date());

        TransactionResponse response = transactionMapper.toResponse(transactionRepository.save(transaction));

        // Recalcula saldo das contas afetadas
        recalcularSaldo(userId, contaAntiga);
        if (!contaAntiga.equals(request.accountId())) {
            recalcularSaldo(userId, request.accountId());
        }
        return response;
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Transaction transaction = buscarTransacaoDoUsuario(userId, id);
        UUID accountId = transaction.getAccountId();

        transaction.setDeletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        recalcularSaldo(userId, accountId);
    }

    // ── Métodos auxiliares ────────────────────────────────────────────────────

    private Transaction buscarTransacaoDoUsuario(UUID userId, UUID id) {
        return transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));
    }

    private void validarContaDoUsuario(UUID userId, UUID accountId) {
        if (!accountRepository.existsByIdAndUserIdAndDeletedAtIsNull(accountId, userId)) {
            throw new BusinessRuleException("Conta não encontrada ou não pertence ao usuário");
        }
    }

    private void recalcularSaldo(UUID userId, UUID accountId) {
        accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId).ifPresent(conta -> {
            BigDecimal novoSaldo = transactionRepository.calculateBalanceByAccountId(accountId);
            conta.setBalance(novoSaldo);
            accountRepository.save(conta);
        });
    }
}
