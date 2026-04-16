package com.fintrack.api.report.service;

import com.fintrack.api.report.dto.CategorySummaryResponse;
import com.fintrack.api.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getExpensesByCategory(UUID userId, LocalDate start, LocalDate end) {
        var expenseRows = transactionRepository.sumExpensesByCategory(userId, start, end);

        BigDecimal totalExpenses = expenseRows.stream()
                .map(row -> row.totalAmount() != null ? row.totalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return List.of();
        }

        return expenseRows.stream()
                .map(row -> new CategorySummaryResponse(
                        row.categoryName(),
                        row.totalAmount(),
                        row.totalAmount()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalExpenses, 2, RoundingMode.HALF_UP)
                                .doubleValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] generateExpensesCsv(UUID userId, LocalDate start, LocalDate end) {
        List<CategorySummaryResponse> summaries = getExpensesByCategory(userId, start, end);

        StringBuilder csv = new StringBuilder();
        csv.append("Categoria;Valor Total;Percentual\n");

        for (var row : summaries) {
            csv.append(String.format("%s;%.2f;%.2f%%\n",
                    row.categoryName(),
                    row.totalAmount(),
                    row.percentage()));
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
