package com.fintrack.api.category.repository;

import com.fintrack.api.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<Category> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<Category> findAllByParentIdAndUserIdAndDeletedAtIsNull(UUID parentId, UUID userId);

    boolean existsByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    /** Verifica se existe subcategoria ativa para impedir exclusão da pai. */
    boolean existsByParentIdAndDeletedAtIsNull(UUID parentId);

    /** Verifica se existe transação usando a categoria (via TransactionRepository — ver CategoryService). */
}
