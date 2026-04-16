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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private UUID userId;
    private UUID categoryId;
    private Category fakeCategory;
    private CategoryResponse fakeResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        fakeCategory = Category.builder()
                .userId(userId)
                .name("Alimentacao")
                .icon("food")
                .color("#FF5733")
                .build();
        fakeResponse = new CategoryResponse(categoryId, userId, null,
                "Alimentacao", "food", "#FF5733", null, null);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Salva categoria raiz e retorna CategoryResponse")
        void create_rootCategory_savesAndReturnsResponse() {
            CategoryRequest request = new CategoryRequest(null, "Alimentacao", "food", "#FF5733");
            when(categoryRepository.save(any())).thenReturn(fakeCategory);
            when(categoryMapper.toResponse(fakeCategory)).thenReturn(fakeResponse);

            CategoryResponse result = categoryService.create(userId, request);

            assertThat(result).isEqualTo(fakeResponse);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Lanca ResourceNotFoundException quando parentId nao existe")
        void create_invalidParentId_throwsResourceNotFoundException() {
            UUID parentId = UUID.randomUUID();
            CategoryRequest request = new CategoryRequest(parentId, "Lanche", null, null);
            when(categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(parentId, userId))
                    .thenReturn(false);

            assertThatThrownBy(() -> categoryService.create(userId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Salva subcategoria quando parentId existe")
        void create_validParentId_savesSubcategory() {
            UUID parentId = UUID.randomUUID();
            CategoryRequest request = new CategoryRequest(parentId, "Lanche", null, null);
            when(categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(parentId, userId))
                    .thenReturn(true);
            when(categoryRepository.save(any())).thenReturn(fakeCategory);
            when(categoryMapper.toResponse(fakeCategory)).thenReturn(fakeResponse);

            categoryService.create(userId, request);

            verify(categoryRepository).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("listAll()")
    class ListAll {

        @Test
        @DisplayName("Retorna lista de categorias do usuario")
        void listAll_returnsUserCategories() {
            when(categoryRepository.findAllByUserIdAndDeletedAtIsNull(userId))
                    .thenReturn(List.of(fakeCategory));
            when(categoryMapper.toResponseList(List.of(fakeCategory)))
                    .thenReturn(List.of(fakeResponse));

            List<CategoryResponse> result = categoryService.listAll(userId);

            assertThat(result).hasSize(1).contains(fakeResponse);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Retorna categoria quando encontrada")
        void findById_found_returnsResponse() {
            when(categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId))
                    .thenReturn(Optional.of(fakeCategory));
            when(categoryMapper.toResponse(fakeCategory)).thenReturn(fakeResponse);

            assertThat(categoryService.findById(userId, categoryId)).isEqualTo(fakeResponse);
        }

        @Test
        @DisplayName("Lanca ResourceNotFoundException quando nao encontrada")
        void findById_notFound_throwsResourceNotFoundException() {
            when(categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(userId, categoryId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Aplica soft delete e limpa referencias em transacoes")
        void delete_noChildren_softDeletesAndClearsReferences() {
            when(categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId))
                    .thenReturn(Optional.of(fakeCategory));
            when(categoryRepository.existsByParentIdAndDeletedAtIsNull(categoryId))
                    .thenReturn(false);

            categoryService.delete(userId, categoryId);

            assertThat(fakeCategory.getDeletedAt()).isNotNull();
            verify(transactionRepository).clearCategoryReferences(userId, categoryId);
            verify(categoryRepository, times(1)).save(fakeCategory);
        }

        @Test
        @DisplayName("Lanca BusinessRuleException quando categoria tem subcategorias ativas")
        void delete_hasActiveChildren_throwsBusinessRuleException() {
            when(categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId))
                    .thenReturn(Optional.of(fakeCategory));
            when(categoryRepository.existsByParentIdAndDeletedAtIsNull(categoryId))
                    .thenReturn(true);

            assertThatThrownBy(() -> categoryService.delete(userId, categoryId))
                    .isInstanceOf(BusinessRuleException.class);
            verify(transactionRepository, never()).clearCategoryReferences(userId, categoryId);
        }

        @Test
        @DisplayName("Lanca ResourceNotFoundException quando categoria nao pertence ao usuario")
        void delete_notFound_throwsResourceNotFoundException() {
            when(categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.delete(userId, categoryId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(transactionRepository, never()).clearCategoryReferences(any(), any());
        }
    }
}
