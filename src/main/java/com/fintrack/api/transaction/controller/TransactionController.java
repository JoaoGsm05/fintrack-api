package com.fintrack.api.transaction.controller;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.shared.dto.PagedResponse;
import com.fintrack.api.transaction.dto.TransactionFilterRequest;
import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.dto.TransactionUpdateRequest;
import com.fintrack.api.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.util.UUID;

@Tag(name = "Transacoes", description = "Lancamentos financeiros com filtros e paginacao")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Criar transacao", description = "Cria uma receita ou despesa vinculada a uma conta.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacao criada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(usuario.getId(), request));
    }

    @Operation(summary = "Listar transacoes", description = "Retorna transacoes paginadas com filtros opcionais por data, conta, categoria, tipo, valor e descricao.")
    @ApiResponse(responseCode = "200", description = "Pagina de transacoes retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedResponse<TransactionResponse>> listAll(
            @AuthenticationPrincipal User usuario,
            @ParameterObject TransactionFilterRequest filter,
            @ParameterObject
            @Parameter(description = "Parametros de paginacao e ordenacao. Exemplo: size=20&sort=date,desc")
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(transactionService.listAll(usuario.getId(), filter, pageable)));
    }

    @Operation(summary = "Buscar transacao por id", description = "Retorna uma transacao especifica pertencente ao usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacao encontrada"),
            @ApiResponse(responseCode = "404", description = "Transacao nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da transacao", example = "33333333-3333-3333-3333-333333333333")
            @PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.findById(usuario.getId(), id));
    }

    @Operation(summary = "Atualizar transacao", description = "Atualiza os dados de uma transacao existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacao atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transacao nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da transacao", example = "33333333-3333-3333-3333-333333333333")
            @PathVariable UUID id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        return ResponseEntity.ok(transactionService.update(usuario.getId(), id, request));
    }

    @Operation(summary = "Remover transacao", description = "Remove logicamente uma transacao do usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transacao removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transacao nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da transacao", example = "33333333-3333-3333-3333-333333333333")
            @PathVariable UUID id) {
        transactionService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
