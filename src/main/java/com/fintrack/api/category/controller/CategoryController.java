package com.fintrack.api.category.controller;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.category.dto.CategoryRequest;
import com.fintrack.api.category.dto.CategoryResponse;
import com.fintrack.api.category.dto.CategoryUpdateRequest;
import com.fintrack.api.category.service.CategoryService;
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

@Tag(name = "Categorias", description = "Categorias hierarquicas para classificar transacoes")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Criar categoria", description = "Cria uma categoria raiz ou filha para organizar transacoes.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(usuario.getId(), request));
    }

    @Operation(summary = "Listar categorias", description = "Retorna todas as categorias ativas do usuario autenticado.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de categorias retornada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class)))
    )
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listAll(@AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(categoryService.listAll(usuario.getId()));
    }

    @Operation(summary = "Buscar categoria por id", description = "Retorna uma categoria especifica pertencente ao usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da categoria", example = "22222222-2222-2222-2222-222222222222")
            @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findById(usuario.getId(), id));
    }

    @Operation(summary = "Atualizar categoria", description = "Atualiza dados visuais e a hierarquia de uma categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da categoria", example = "22222222-2222-2222-2222-222222222222")
            @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.update(usuario.getId(), id, request));
    }

    @Operation(summary = "Remover categoria", description = "Remove logicamente uma categoria do usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @Parameter(description = "Identificador da categoria", example = "22222222-2222-2222-2222-222222222222")
            @PathVariable UUID id) {
        categoryService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
