package com.fintrack.api.budget.controller;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.budget.dto.BudgetRequest;
import com.fintrack.api.budget.dto.BudgetResponse;
import com.fintrack.api.budget.service.BudgetService;
import com.fintrack.api.shared.dto.PagedResponse;
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

@Tag(name = "Orcamentos", description = "CRUD de budgets por categoria e periodo")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "Criar orcamento", description = "Cria um budget para uma categoria em um periodo definido.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orcamento criado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados invalidos ou regra de negocio violada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<BudgetResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.create(usuario.getId(), request));
    }

    @Operation(summary = "Listar orcamentos", description = "Retorna budgets paginados do usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Pagina de orcamentos retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedResponse<BudgetResponse>> listAll(
            @AuthenticationPrincipal User usuario,
            @ParameterObject
            @Parameter(description = "Parametros de paginacao e ordenacao. Exemplo: size=20&sort=startDate,desc")
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(budgetService.listAll(usuario.getId(), pageable)));
    }

    @Operation(summary = "Buscar orcamento por id", description = "Retorna um budget especifico pertencente ao usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orcamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Orcamento nao encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> findById(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador do orcamento", example = "44444444-4444-4444-4444-444444444444")
            @PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.findById(usuario.getId(), id));
    }

    @Operation(summary = "Atualizar orcamento", description = "Atualiza valor, periodo e intervalo de um budget existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orcamento atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Orcamento nao encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Dados invalidos ou regra de negocio violada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador do orcamento", example = "44444444-4444-4444-4444-444444444444")
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.update(usuario.getId(), id, request));
    }

    @Operation(summary = "Remover orcamento", description = "Remove logicamente um budget do usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Orcamento removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Orcamento nao encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador do orcamento", example = "44444444-4444-4444-4444-444444444444")
            @PathVariable UUID id) {
        budgetService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
