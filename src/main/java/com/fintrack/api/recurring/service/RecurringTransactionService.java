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
import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final RecurringTransactionMapper recurringTransactionMapper;
    private final TransactionService transactionService;

    @Transactional
    public RecurringTransactionResponse create(UUID userId, RecurringTransactionRequest request) {
        validarContaECategoria(userId, request.accountId(), request.categoryId());
        
        RecurringTransaction entity = recurringTransactionMapper.toEntity(request);
        entity.setUserId(userId);
        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> listAll(UUID userId) {
        return recurringTransactionRepository.findAllByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(recurringTransactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecurringTransactionResponse findById(UUID userId, UUID id) {
        return recurringTransactionMapper.toResponse(buscarRecorrenteDoUsuario(userId, id));
    }

    @Transactional
    public RecurringTransactionResponse update(UUID userId, UUID id, RecurringTransactionRequest request) {
        RecurringTransaction entity = buscarRecorrenteDoUsuario(userId, id);
        
        validarContaECategoria(userId, request.accountId(), request.categoryId());

        entity.setAccountId(request.accountId());
        entity.setCategoryId(request.categoryId());
        entity.setType(request.type());
        entity.setAmount(request.amount());
        entity.setDescription(request.description());
        entity.setFrequency(request.frequency());
        entity.setNextOccurrence(request.nextOccurrence());

        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(entity));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        RecurringTransaction entity = buscarRecorrenteDoUsuario(userId, id);
        entity.setDeletedAt(LocalDateTime.now());
        recurringTransactionRepository.save(entity);
    }

    @Transactional
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository
                .findDueTransactionsForUpdate(today);

        log.info("Processando {} transações recorrentes vencidas.", dueTransactions.size());

        for (RecurringTransaction rt : dueTransactions) {
            try {
                processSingleRecurringTransaction(rt);
            } catch (Exception e) {
                log.error("Erro ao processar transação recorrente {}: {}", rt.getId(), e.getMessage());
            }
        }
    }

    private void processSingleRecurringTransaction(RecurringTransaction rt) {
        // 1. Criar a transação real
        TransactionRequest transactionRequest = new TransactionRequest(
                rt.getAccountId(),
                rt.getCategoryId(),
                rt.getType(),
                rt.getAmount(),
                rt.getDescription(),
                rt.getNextOccurrence() // Usa a data que deveria ter ocorrido
        );

        transactionService.create(rt.getUserId(), transactionRequest);

        // 2. Atualizar a próxima ocorrência
        rt.setNextOccurrence(calculateNextOccurrence(rt.getNextOccurrence(), rt.getFrequency()));

        // 3. Salvar alteração
        recurringTransactionRepository.save(rt);
    }

    private LocalDate calculateNextOccurrence(LocalDate current, RecurringFrequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }

    private RecurringTransaction buscarRecorrenteDoUsuario(UUID userId, UUID id) {
        return recurringTransactionRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação Recorrente", id));
    }

    private void validarContaECategoria(UUID userId, UUID accountId, UUID categoryId) {
        if (!accountRepository.existsByIdAndUserIdAndDeletedAtIsNull(accountId, userId)) {
            throw new BusinessRuleException("Conta não encontrada ou não pertence ao usuário");
        }
        if (categoryId != null && !categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)) {
            throw new BusinessRuleException("Categoria não encontrada ou não pertence ao usuário");
        }
    }
}
