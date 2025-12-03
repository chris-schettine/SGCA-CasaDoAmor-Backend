package br.com.casadoamor.sgca.modules.agendamento.entity;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AgendamentoTest {

    @Test
    @DisplayName("Deve cancelar agendamento corretamente")
    void deveCancelarAgendamento() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        AuthUsuario usuario = new AuthUsuario();

        agendamento.cancelar("Motivo teste", usuario);

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        assertThat(agendamento.getMotivoCancelamento()).isEqualTo("Motivo teste");
        assertThat(agendamento.getCanceledAt()).isNotNull();
        assertThat(agendamento.getCanceledBy()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("Deve remarcar agendamento corretamente")
    void deveRemarcarAgendamento() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        LocalDateTime novaDataInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime novaDataFim = novaDataInicio.plusHours(1);

        agendamento.remarcar(novaDataInicio, novaDataFim);

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.REMARCADO);
        assertThat(agendamento.getDataInicio()).isEqualTo(novaDataInicio);
        assertThat(agendamento.getDataFim()).isEqualTo(novaDataFim);
    }

    @Test
    @DisplayName("Deve confirmar agendamento corretamente")
    void deveConfirmarAgendamento() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        agendamento.confirmar();

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
        assertThat(agendamento.getConfirmacaoPaciente()).isTrue();
        assertThat(agendamento.getConfirmacaoPacienteEm()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar UUID e Data de Criação ao persistir")
    void deveGerarUuidEDataCriacao() {
        Agendamento agendamento = new Agendamento();

        agendamento.onCreate();

        assertThat(agendamento.getUuid()).isNotNull();
        assertThat(agendamento.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve atualizar Data de Atualização ao atualizar")
    void deveAtualizarDataAtualizacao() {
        Agendamento agendamento = new Agendamento();

        agendamento.onUpdate();

        assertThat(agendamento.getUpdatedAt()).isNotNull();
    }
}
