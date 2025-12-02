package br.com.casadoamor.sgca.modules.agendamento.controller;

import br.com.casadoamor.sgca.modules.agendamento.dto.EstatisticasAgendamentoDTO;
import br.com.casadoamor.sgca.modules.agendamento.service.EstatisticasAgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/agendamentos/estatisticas")
@RequiredArgsConstructor
@Tag(name = "Estatísticas de Agendamentos", description = "Endpoints para visualizar estatísticas e métricas de agendamentos para dashboard")
public class EstatisticasAgendamentoController {

    private final EstatisticasAgendamentoService estatisticasAgendamentoService;

    @Operation(
        summary = "Obter estatísticas gerais de agendamentos",
        description = "Retorna um conjunto completo de estatísticas e métricas sobre agendamentos de pacientes e acompanhantes, " +
                     "incluindo totais, distribuições, taxas de comparecimento, top profissionais, top serviços e muito mais. " +
                     "Ideal para exibição em dashboard administrativo."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO', 'NUTRICIONISTA', 'DENTISTA')")
    public ResponseEntity<EstatisticasAgendamentoDTO> obterEstatisticasGerais() {
        log.info("Requisição para obter estatísticas gerais de agendamentos");
        EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();
        return ResponseEntity.ok(estatisticas);
    }
}
