package com.fintrack.api.budget.service;

import com.fintrack.api.budget.dto.BudgetMapper;
import com.fintrack.api.budget.dto.BudgetRequest;
import com.fintrack.api.budget.dto.BudgetResponse;
import com.fintrack.api.budget.dto.BudgetSnapshot;
import com.fintrack.api.budget.entity.Budget;
import com.fintrack.api.budget.entity.BudgetPeriod;
import com.fintrack.api.budget.repository.BudgetRepository;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetService budgetService;

    private UUID userId;
    private UUID budgetId;
    private UUID categoryId;
    private Budget fakeBudget;
    private BudgetSnapshot fakeSnapshot;
    private BudgetResponse fakeResponse;
    private final LocalDate startDate = LocalDate.of(2026, 4, 1);
    private final LocalDate endDate = LocalDate.of(2026, 4, 30);

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        budgetId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        fakeBudget = Budget.builder()
                .id(budgetId)
                .userId(userId)
                .categoryId(categoryId)
                .amount(new BigDecimal("500.00"))
                .period(BudgetPeriod.MONTHLY)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        fakeSnapshot = new BudgetSnapshot(
                budgetId,
                userId,
                categoryId,
                "Alimentacao",
                new BigDecimal("500.00"),
                BudgetPeriod.MONTHLY,
                startDate,
                endDate,
                new BigDecimal("100.00")
        );

        fakeResponse = new BudgetResponse(
                budgetId,
                userId,
                categoryId,
                "Alimentacao",
                new BigDecimal("500.00"),
                BudgetPeriod.MONTHLY,
                startDate,
                endDate,
                new BigDecimal("100.00"),
                new BigDecimal("400.00")
        );
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Salva orcamento e retorna BudgetResponse agregado")
        void create_validRequest_savesAndReturnsResponse() {
            BudgetRequest request = new BudgetRequest(categoryId, new BigDecimal("500.00"),
                    BudgetPeriod.MONTHLY, startDate, endDate);

            when(categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)).thenReturn(true);
            when(budgetRepository.save(any())).thenReturn(fakeBudget);
            when(budgetRepository.findBudgetSnapshotByIdAndUserId(budgetId, userId))
                    .thenReturn(Optional.of(fakeSnapshot));
            when(budgetMapper.toResponse(fakeSnapshot)).thenReturn(fakeResponse);

            BudgetResponse result = budgetService.create(userId, request);

            assertThat(result).isEqualTo(fakeResponse);
            verify(budgetRepository).save(any(Budget.class));
        }

        @Test
        @DisplayName("Lanca ResourceNotFoundException quando categoria nao pertence ao usuario")
        void create_invalidCategory_throwsResourceNotFoundException() {
            BudgetRequest request = new BudgetRequest(categoryId, new BigDecimal("500.00"),
                    BudgetPeriod.MONTHLY, startDate, endDate);

            when(categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)).thenReturn(false);

            assertThatThrownBy(() -> budgetService.create(userId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(budgetRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listAll()")
    class ListAll {

        @Test
        @DisplayName("Retorna pagina de orcamentos agregados do usuario")
        void listAll_returnsAggregatedPage() {
            PageRequest pageable = PageRequest.of(0, 20);
            when(budgetRepository.findBudgetSnapshotsByUserId(userId, pageable))
                    .thenReturn(new PageImpl<>(List.of(fakeSnapshot), pageable, 1));
            when(budgetMapper.toResponse(fakeSnapshot)).thenReturn(fakeResponse);

            var result = budgetService.listAll(userId, pageable);

            assertThat(result.getContent()).hasSize(1).contains(fakeResponse);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Retorna pagina vazia quando usuario nao tem orcamentos")
        void listAll_noBudgets_returnsEmptyPage() {
            PageRequest pageable = PageRequest.of(0, 20);
            when(budgetRepository.findBudgetSnapshotsByUserId(userId, pageable))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            assertThat(budgetService.listAll(userId, pageable).getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("listAllByCategory()")
    class ListAllByCategory {

        @Test
        @DisplayName("Retorna apenas orcamentos da categoria informada")
        void listAllByCategory_returnsAggregatedList() {
            when(budgetRepository.findBudgetSnapshotsByUserIdAndCategoryId(userId, categoryId))
                    .thenReturn(List.of(fakeSnapshot));
            when(budgetMapper.toResponse(fakeSnapshot)).thenReturn(fakeResponse);

            List<BudgetResponse> result = budgetService.listAllByCategory(userId, categoryId);

            assertThat(result).hasSize(1).contains(fakeResponse);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Retorna orcamento agregado quando encontrado")
        void findById_found_returnsResponse() {
            when(budgetRepository.findBudgetSnapshotByIdAndUserId(budgetId, userId))
                    .thenReturn(Optional.of(fakeSnapshot));
            when(budgetMapper.toResponse(fakeSnapshot)).thenReturn(fakeResponse);

            BudgetResponse result = budgetService.findById(userId, budgetId);

            assertThat(result).isEqualTo(fakeResponse);
        }

        @Test
        @DisplayName("Lanca ResourceNotFoundException quando nao encontrado")
        void findById_notFound_throwsResourceNotFoundException() {
            when(budgetRepository.findBudgetSnapshotByIdAndUserId(budgetId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> budgetService.findById(userId, budgetId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Aplica soft delete no orcamento")
        void delete_found_softDeletes() {
            when(budgetRepository.findByIdAndUserIdAndDeletedAtIsNull(budgetId, userId))
                    .thenReturn(Optional.of(fakeBudget));

            budgetService.delete(userId, budgetId);

            assertThat(fakeBudget.getDeletedAt()).isNotNull();
            verify(budgetRepository).save(fakeBudget);
        }

        @Test
        @DisplayName("Lanca ResourceNotFoundException quando orcamento nao encontrado")
        void delete_notFound_throwsResourceNotFoundException() {
            when(budgetRepository.findByIdAndUserIdAndDeletedAtIsNull(budgetId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> budgetService.delete(userId, budgetId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(budgetRepository, never()).save(any());
        }
    }
}
