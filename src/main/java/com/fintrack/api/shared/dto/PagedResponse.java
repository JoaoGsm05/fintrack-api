package com.fintrack.api.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(name = "PagedResponse", description = "Envelope padrao para respostas paginadas")
public record PagedResponse<T>(
        @Schema(description = "Itens da pagina atual")
        List<T> content,
        @Schema(description = "Total de registros encontrados", example = "42")
        long totalElements,
        @Schema(description = "Quantidade total de paginas", example = "3")
        int totalPages,
        @Schema(description = "Tamanho da pagina", example = "20")
        int size,
        @Schema(description = "Indice da pagina atual, iniciando em zero", example = "0")
        int number,
        @Schema(description = "Indica se esta e a primeira pagina", example = "true")
        boolean first,
        @Schema(description = "Indica se esta e a ultima pagina", example = "false")
        boolean last,
        @Schema(description = "Indica se a pagina esta vazia", example = "false")
        boolean empty
) {

    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
