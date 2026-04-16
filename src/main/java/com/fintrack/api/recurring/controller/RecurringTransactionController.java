package com.fintrack.api.recurring.controller;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.recurring.dto.RecurringTransactionRequest;
import com.fintrack.api.recurring.dto.RecurringTransactionResponse;
import com.fintrack.api.recurring.service.RecurringTransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Recorrências", description = "Transações recorrentes agendadas automaticamente por frequência")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recurringTransactionService.create(usuario.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> listAll(
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(recurringTransactionService.listAll(usuario.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> findById(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        return ResponseEntity.ok(recurringTransactionService.findById(usuario.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> update(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id,
            @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.ok(recurringTransactionService.update(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        recurringTransactionService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
