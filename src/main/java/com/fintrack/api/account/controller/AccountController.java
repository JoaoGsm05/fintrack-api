package com.fintrack.api.account.controller;

import com.fintrack.api.account.dto.AccountRequest;
import com.fintrack.api.account.dto.AccountResponse;
import com.fintrack.api.account.dto.AccountUpdateRequest;
import com.fintrack.api.account.service.AccountService;
import com.fintrack.api.auth.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.create(usuario.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAll(@AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(accountService.listAll(usuario.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.findById(usuario.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id,
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.update(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        accountService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
