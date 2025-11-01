package br.com.casadoamor.sgca.modules.common.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record PaginatedResponseDTO<T>(
        List<T> nodes,
        boolean hasNextPage,
        boolean hasPreviousPage,
        int totalCount) {
}
