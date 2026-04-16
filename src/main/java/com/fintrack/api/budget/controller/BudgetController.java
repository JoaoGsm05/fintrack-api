package com.fintrack.api.budget.controller;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.budget.dto.BudgetRequest;
import com.fintrack.api.budget.dto.BudgetResponse;
import com.fintrack.api.budget.service.BudgetService;
import com.fintrack.api.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Orçamentos", description = "CRUD de budgets por categoria e período")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.create(usuario.getId(), request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<BudgetResponse>> listAll(
            @AuthenticationPrincipal User usuario,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(budgetService.listAll(usuario.getId(), pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> findById(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.findById(usuario.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.update(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        budgetService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
