package br.com.casadoamor.sgca.modules.hospedagem.entity;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto;

class QuartoTest {

    @Test
    void temVagasDisponiveis_true_whenActiveNotMaintenanceAndLessOcupada() {
        Quarto q = Quarto.builder()
                .ativo(true)
                .emManutencao(false)
                .capacidadeTotal(4)
                .capacidadeOcupada(2)
                .build();

        assertThat(q.temVagasDisponiveis()).isTrue();
        assertThat(q.getVagasDisponiveis()).isEqualTo(2);
    }

    @Test
    void temVagasDisponiveis_false_whenInactiveOrMaintenanceOrFull() {
        Quarto q1 = Quarto.builder().ativo(false).emManutencao(false).capacidadeTotal(2).capacidadeOcupada(1).build();
        Quarto q2 = Quarto.builder().ativo(true).emManutencao(true).capacidadeTotal(2).capacidadeOcupada(1).build();
        Quarto q3 = Quarto.builder().ativo(true).emManutencao(false).capacidadeTotal(2).capacidadeOcupada(2).build();

        assertThat(q1.temVagasDisponiveis()).isFalse();
        assertThat(q2.temVagasDisponiveis()).isFalse();
        assertThat(q3.temVagasDisponiveis()).isFalse();
    }

    @Test
    void incrementarAndDecrementarOcupacao_respectsBounds() {
        Quarto q = Quarto.builder().capacidadeTotal(2).capacidadeOcupada(0).build();

        q.incrementarOcupacao();
        assertThat(q.getCapacidadeOcupada()).isEqualTo(1);

        q.incrementarOcupacao();
        assertThat(q.getCapacidadeOcupada()).isEqualTo(2);

        // cannot exceed total
        q.incrementarOcupacao();
        assertThat(q.getCapacidadeOcupada()).isEqualTo(2);

        q.decrementarOcupacao();
        assertThat(q.getCapacidadeOcupada()).isEqualTo(1);

        q.decrementarOcupacao();
        assertThat(q.getCapacidadeOcupada()).isEqualTo(0);

        // won't go below 0
        q.decrementarOcupacao();
        assertThat(q.getCapacidadeOcupada()).isEqualTo(0);
    }

    @Test
    void lifecycle_onCreate_setsUuidAndCreatedAt_and_onUpdate_setsUpdatedAt() {
        Quarto q = Quarto.builder().nome("T1").tipo(TipoQuarto.COMPARTILHADO).ala(AlaQuarto.MISTA).build();

        // PrePersist behaviour
        q.onCreate();
        assertThat(q.getUuid()).isNotNull();
        assertThat(q.getCreatedAt()).isNotNull();

        // PreUpdate behaviour
        q.onUpdate();
        assertThat(q.getUpdatedAt()).isNotNull();
        assertThat(q.getUpdatedAt()).isAfterOrEqualTo(q.getCreatedAt());
    }
}
