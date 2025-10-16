package br.com.casadoamor.sgca.mapper.common;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.dto.common.PaginatedResponseDTO;

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
