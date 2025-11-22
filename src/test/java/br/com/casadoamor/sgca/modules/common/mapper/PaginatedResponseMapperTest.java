package br.com.casadoamor.sgca.modules.common.mapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;

class PaginatedResponseMapperTest {

  private final PaginatedResponseMapper mapper = new PaginatedResponseMapper();

  @Test
  void toDTO_buildsCorrectResponse() {
    var items = List.of("a", "b", "c");

    PaginatedResponseDTO<String> dto = mapper.toDTO(items, 10L, true, false);

    assertThat(dto.nodes()).containsExactly("a", "b", "c");
    assertThat(dto.totalCount()).isEqualTo(10);
    assertThat(dto.hasPreviousPage()).isTrue();
    assertThat(dto.hasNextPage()).isFalse();
  }

  @Test
  void toDTO_handlesEmptyLists() {
    List<String> items = List.<String>of();

    PaginatedResponseDTO<String> dto = mapper.toDTO(items, 0L, false, false);

    assertThat(dto.nodes()).isEmpty();
    assertThat(dto.totalCount()).isEqualTo(0);
  }
}
