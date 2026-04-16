package com.fintrack.api.account.controller;

import com.fintrack.api.account.dto.AccountRequest;
import com.fintrack.api.account.dto.AccountResponse;
import com.fintrack.api.account.dto.AccountUpdateRequest;
import com.fintrack.api.account.service.AccountService;
import com.fintrack.api.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Contas", description = "Gerenciamento de contas financeiras do usuario")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Criar conta", description = "Cria uma nova conta financeira para o usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.create(usuario.getId(), request));
    }

    @Operation(summary = "Listar contas", description = "Retorna todas as contas ativas do usuario autenticado.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de contas retornada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountResponse.class)))
    )
    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAll(@AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(accountService.listAll(usuario.getId()));
    }

    @Operation(summary = "Buscar conta por id", description = "Retorna uma conta especifica pertencente ao usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da conta", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.findById(usuario.getId(), id));
    }

    @Operation(summary = "Atualizar conta", description = "Atualiza nome, tipo e moeda de uma conta existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da conta", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID id,
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.update(usuario.getId(), id, request));
    }

    @Operation(summary = "Remover conta", description = "Remove logicamente uma conta do usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da conta", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID id) {
        accountService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
