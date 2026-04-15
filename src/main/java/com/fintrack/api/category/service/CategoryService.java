package com.fintrack.api.category.service;

import com.fintrack.api.category.dto.CategoryMapper;
import com.fintrack.api.category.dto.CategoryRequest;
import com.fintrack.api.category.dto.CategoryResponse;
import com.fintrack.api.category.dto.CategoryUpdateRequest;
import com.fintrack.api.category.entity.Category;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.shared.exception.BusinessRuleException;
import com.fintrack.api.shared.exception.ResourceNotFoundException;
import com.fintrack.api.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponse create(UUID userId, CategoryRequest request) {
        // Valida a categoria pai, se informada
        if (request.parentId() != null
                && !categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(request.parentId(), userId)) {
            throw new ResourceNotFoundException("Categoria pai", request.parentId());
        }

        Category category = Category.builder()
                .userId(userId)
                .parentId(request.parentId())
                .name(request.name())
                .icon(request.icon())
                .color(request.color())
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll(UUID userId) {
        return categoryMapper.toResponseList(categoryRepository.findAllByUserIdAndDeletedAtIsNull(userId));
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(UUID userId, UUID id) {
        return categoryMapper.toResponse(buscarCategoriaDoUsuario(userId, id));
    }

    @Transactional
    public CategoryResponse update(UUID userId, UUID id, CategoryUpdateRequest request) {
        Category category = buscarCategoriaDoUsuario(userId, id);
        if (request.name() != null)  category.setName(request.name());
        if (request.icon() != null)  category.setIcon(request.icon());
        if (request.color() != null) category.setColor(request.color());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        buscarCategoriaDoUsuario(userId, id); // garante posse

        if (categoryRepository.existsByParentIdAndDeletedAtIsNull(id)) {
            throw new BusinessRuleException(
                    "Não é possível excluir a categoria pois ela possui subcategorias ativas");
        }

        if (transactionRepository.existsByCategoryIdAndDeletedAtIsNull(id)) {
            throw new BusinessRuleException(
                    "Não é possível excluir a categoria pois ela possui transações ativas");
        }

        Category category = buscarCategoriaDoUsuario(userId, id);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    // ── Método auxiliar ───────────────────────────────────────────────────────

    private Category buscarCategoriaDoUsuario(UUID userId, UUID id) {
        return categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }
}
