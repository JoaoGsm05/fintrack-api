package com.fintrack.api.recurring.controller;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.recurring.dto.RecurringTransactionRequest;
import com.fintrack.api.recurring.dto.RecurringTransactionResponse;
import com.fintrack.api.recurring.service.RecurringTransactionService;
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

@Tag(name = "Recorrencias", description = "Transacoes recorrentes agendadas automaticamente por frequencia")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @Operation(summary = "Criar recorrencia", description = "Agenda uma transacao recorrente para geracao automatica futura.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recorrencia criada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recurringTransactionService.create(usuario.getId(), request));
    }

    @Operation(summary = "Listar recorrencias", description = "Retorna todas as recorrencias ativas do usuario autenticado.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de recorrencias retornada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecurringTransactionResponse.class)))
    )
    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> listAll(
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(recurringTransactionService.listAll(usuario.getId()));
    }

    @Operation(summary = "Buscar recorrencia por id", description = "Retorna uma recorrencia especifica pertencente ao usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recorrencia encontrada"),
            @ApiResponse(responseCode = "404", description = "Recorrencia nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> findById(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da recorrencia", example = "55555555-5555-5555-5555-555555555555")
            @PathVariable UUID id) {
        return ResponseEntity.ok(recurringTransactionService.findById(usuario.getId(), id));
    }

    @Operation(summary = "Atualizar recorrencia", description = "Atualiza dados operacionais de uma recorrencia existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recorrencia atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recorrencia nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> update(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da recorrencia", example = "55555555-5555-5555-5555-555555555555")
            @PathVariable UUID id,
            @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.ok(recurringTransactionService.update(usuario.getId(), id, request));
    }

    @Operation(summary = "Remover recorrencia", description = "Remove logicamente uma recorrencia do usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Recorrencia removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recorrencia nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da recorrencia", example = "55555555-5555-5555-5555-555555555555")
            @PathVariable UUID id) {
        recurringTransactionService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
