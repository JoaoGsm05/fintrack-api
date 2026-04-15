package com.fintrack.api.category.controller;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.category.dto.CategoryRequest;
import com.fintrack.api.category.dto.CategoryResponse;
import com.fintrack.api.category.dto.CategoryUpdateRequest;
import com.fintrack.api.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(usuario.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listAll(@AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(categoryService.listAll(usuario.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findById(usuario.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.update(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User usuario,
            @PathVariable UUID id) {
        categoryService.delete(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
