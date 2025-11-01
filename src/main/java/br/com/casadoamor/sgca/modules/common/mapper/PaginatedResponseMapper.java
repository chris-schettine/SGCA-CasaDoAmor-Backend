package br.com.casadoamor.sgca.modules.common.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;

@Component
public class PaginatedResponseMapper {
  public <T> PaginatedResponseDTO<T> toDTO(List<T> items, long totalItems, boolean hasPreviousPage, boolean hasNextPage) {
    return PaginatedResponseDTO.<T>builder()
      .nodes(items)       
      .totalCount((int) totalItems)  
      .hasPreviousPage(hasPreviousPage)       
      .hasNextPage(hasNextPage)      
      .build();
  }
}
