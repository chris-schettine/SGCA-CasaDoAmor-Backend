package br.com.casadoamor.sgca.modules.paciente.controller;

import br.com.casadoamor.sgca.modules.paciente.dto.EstatisticasPacienteAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.paciente.service.EstatisticasPacienteAcompanhanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes/estatisticas")
@RequiredArgsConstructor
@Tag(name = "Estatísticas de Pacientes e Acompanhantes", description = "Endpoints para visualização de estatísticas e dashboard")
@SecurityRequirement(name = "bearer-key")
public class EstatisticasPacienteAcompanhanteController {

    private final EstatisticasPacienteAcompanhanteService estatisticasService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'ENFERMEIRO', 'DENTISTA', 'FISIOTERAPEUTA', 'NUTRICIONISTA', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(
            summary = "Obter estatísticas completas do dashboard",
            description = "Retorna estatísticas completas de pacientes e acompanhantes para exibição no dashboard. " +
                    "Inclui: totais, ativos/inativos, registros por período, status, relacionamentos, " +
                    "dados clínicos, distribuição geográfica, tendências temporais e muito mais."
    )
    public ResponseEntity<EstatisticasPacienteAcompanhanteDTO> obterEstatisticasDashboard() {
        EstatisticasPacienteAcompanhanteDTO estatisticas = estatisticasService.obterEstatisticasGerais();
        return ResponseEntity.ok(estatisticas);
    }
}
