package com.fintrack.api.report.controller;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.report.dto.CategorySummaryResponse;
import com.fintrack.api.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Relatórios", description = "Relatórios analíticos e exportação de dados")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Despesas por categoria",
               description = "Retorna total e percentual de despesas agrupadas por categoria no período informado.")
    @GetMapping("/expenses-by-category")
    public ResponseEntity<List<CategorySummaryResponse>> getExpensesByCategory(
            @AuthenticationPrincipal User usuario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(reportService.getExpensesByCategory(usuario.getId(), start, end));
    }

    @Operation(summary = "Exportar despesas em CSV",
               description = "Gera arquivo CSV com todas as despesas do período. O download inicia automaticamente.")
    @GetMapping("/export-expenses-csv")
    public ResponseEntity<byte[]> exportExpensesCsv(
            @AuthenticationPrincipal User usuario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        byte[] csvData = reportService.generateExpensesCsv(usuario.getId(), start, end);
        String filename = String.format("relatorio_despesas_%s_a_%s.csv", start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
