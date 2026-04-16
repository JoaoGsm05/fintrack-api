package com.fintrack.api.transaction.controller;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.transaction.dto.TransactionFilterRequest;
import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.dto.TransactionUpdateRequest;
import com.fintrack.api.transaction.service.TransactionService;
import com.fintrack.api.shared.dto.PagedResponse;
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

@Tag(name = "Transações", description = "Lançamentos financeiros (receitas e despesas) com filtros e paginação")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(usuario.getId(), request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TransactionResponse>> listAll(
            @AuthenticationPrincipal User usuario,
            TransactionFilterRequest filter,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(transactionService.listAll(usuario.getId(), filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.findById(usuario.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        return ResponseEntity.ok(transactionService.update(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        transactionService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
