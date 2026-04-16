package com.fintrack.api.budget.service;

import com.fintrack.api.budget.dto.BudgetMapper;
import com.fintrack.api.budget.dto.BudgetRequest;
import com.fintrack.api.budget.dto.BudgetResponse;
import com.fintrack.api.budget.entity.Budget;
import com.fintrack.api.budget.repository.BudgetRepository;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    @Transactional
    public BudgetResponse create(UUID userId, BudgetRequest request) {
        validarCategoria(userId, request.categoryId());

        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .amount(request.amount())
                .period(request.period())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        Budget savedBudget = budgetRepository.save(budget);
        return budgetRepository.findBudgetSnapshotByIdAndUserId(savedBudget.getId(), userId)
                .map(budgetMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Orcamento", savedBudget.getId()));
    }

    @Transactional(readOnly = true)
    public Page<BudgetResponse> listAll(UUID userId, Pageable pageable) {
        return budgetRepository.findBudgetSnapshotsByUserId(userId, pageable)
                .map(budgetMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> listAllByCategory(UUID userId, UUID categoryId) {
        return budgetRepository.findBudgetSnapshotsByUserIdAndCategoryId(userId, categoryId).stream()
                .map(budgetMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse findById(UUID userId, UUID id) {
        return budgetRepository.findBudgetSnapshotByIdAndUserId(id, userId)
                .map(budgetMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Orcamento", id));
    }

    @Transactional
    public BudgetResponse update(UUID userId, UUID id, BudgetRequest request) {
        Budget budget = buscarBudgetDoUsuario(userId, id);
        validarCategoria(userId, request.categoryId());

        budget.setCategoryId(request.categoryId());
        budget.setAmount(request.amount());
        budget.setPeriod(request.period());
        budget.setStartDate(request.startDate());
        budget.setEndDate(request.endDate());
        budgetRepository.save(budget);

        return budgetRepository.findBudgetSnapshotByIdAndUserId(id, userId)
                .map(budgetMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Orcamento", id));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Budget budget = buscarBudgetDoUsuario(userId, id);
        budget.setDeletedAt(LocalDateTime.now());
        budgetRepository.save(budget);
    }

    @Transactional
    public boolean markAlert80AsSent(UUID userId, UUID budgetId) {
        return budgetRepository.markAlert80AsSent(budgetId, userId, LocalDateTime.now()) > 0;
    }

    @Transactional
    public boolean markAlert100AsSent(UUID userId, UUID budgetId) {
        return budgetRepository.markAlert100AsSent(budgetId, userId, LocalDateTime.now()) > 0;
    }

    private Budget buscarBudgetDoUsuario(UUID userId, UUID id) {
        return budgetRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Orcamento", id));
    }

    private void validarCategoria(UUID userId, UUID categoryId) {
        if (!categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)) {
            throw new ResourceNotFoundException("Categoria", categoryId);
        }
    }
}
