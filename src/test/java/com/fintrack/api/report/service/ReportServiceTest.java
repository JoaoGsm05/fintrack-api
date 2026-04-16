package com.fintrack.api.report.service;

import com.fintrack.api.report.dto.CategoryExpenseAggregate;
import com.fintrack.api.report.dto.CategorySummaryResponse;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private UUID userId;
    private final LocalDate start = LocalDate.of(2026, 4, 1);
    private final LocalDate end = LocalDate.of(2026, 4, 30);

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("getExpensesByCategory()")
    class GetExpensesByCategory {

        @Test
        @DisplayName("Retorna summaries com percentual calculado quando ha despesas")
        void getExpenses_withData_returnsSummaries() {
            when(transactionRepository.sumExpensesByCategory(userId, start, end))
                    .thenReturn(List.of(
                            new CategoryExpenseAggregate("Alimentacao", new BigDecimal("300.00")),
                            new CategoryExpenseAggregate("Outros", new BigDecimal("100.00"))
                    ));

            List<CategorySummaryResponse> result = reportService.getExpensesByCategory(userId, start, end);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).categoryName()).isEqualTo("Alimentacao");
            assertThat(result.get(0).percentage()).isEqualTo(75.0);
            assertThat(result.get(1).categoryName()).isEqualTo("Outros");
            assertThat(result.get(1).percentage()).isEqualTo(25.0);
        }

        @Test
        @DisplayName("Retorna lista vazia quando nao ha despesas no periodo")
        void getExpenses_noExpenses_returnsEmpty() {
            when(transactionRepository.sumExpensesByCategory(userId, start, end))
                    .thenReturn(List.of());

            List<CategorySummaryResponse> result = reportService.getExpensesByCategory(userId, start, end);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("generateExpensesCsv()")
    class GenerateCsv {

        @Test
        @DisplayName("Gera CSV com cabecalho quando ha dados")
        void generateCsv_withData_returnsValidCsv() {
            when(transactionRepository.sumExpensesByCategory(userId, start, end))
                    .thenReturn(List.of(new CategoryExpenseAggregate("Outros", new BigDecimal("200.00"))));

            byte[] csvBytes = reportService.generateExpensesCsv(userId, start, end);
            String csv = new String(csvBytes, java.nio.charset.StandardCharsets.UTF_8);

            assertThat(csv).startsWith("Categoria;Valor Total;Percentual");
            assertThat(csv).contains("Outros");
        }

        @Test
        @DisplayName("Gera CSV apenas com cabecalho quando nao ha dados")
        void generateCsv_noData_returnsHeaderOnly() {
            when(transactionRepository.sumExpensesByCategory(userId, start, end))
                    .thenReturn(List.of());

            byte[] csvBytes = reportService.generateExpensesCsv(userId, start, end);
            String csv = new String(csvBytes, java.nio.charset.StandardCharsets.UTF_8);

            assertThat(csv).isEqualTo("Categoria;Valor Total;Percentual\n");
        }
    }
}
