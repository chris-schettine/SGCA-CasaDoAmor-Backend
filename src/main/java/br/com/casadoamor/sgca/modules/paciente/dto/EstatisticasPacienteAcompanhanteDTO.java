package br.com.casadoamor.sgca.modules.paciente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasPacienteAcompanhanteDTO {

    // ===== ESTATÍSTICAS GERAIS DE PACIENTES =====
    private Long totalPacientes;
    private Long pacientesAtivos;
    private Long pacientesInativos;
    private Long pacientesRegistradosHoje;
    private Long pacientesRegistradosSemana;
    private Long pacientesRegistradosMes;
    private Long pacientesRegistradosAno;
    
    // ===== ESTATÍSTICAS POR STATUS DE PACIENTE =====
    private Long pacientesEmTratamento;
    private Long pacientesCurados;
    private Long pacientesEmObservacao;
    private Long pacientesFalecidos;
    
    // ===== ESTATÍSTICAS GERAIS DE ACOMPANHANTES =====
    private Long totalAcompanhantes;
    private Long acompanhantesAtivos;
    private Long acompanhantesInativos;
    private Long acompanhantesRegistradosHoje;
    private Long acompanhantesRegistradosSemana;
    private Long acompanhantesRegistradosMes;
    private Long acompanhantesRegistradosAno;
    
    // ===== ESTATÍSTICAS DE RELACIONAMENTO =====
    private Double mediaAcompanhantesPorPaciente;
    private Long pacientesSemAcompanhante;
    private Long pacientesComUmAcompanhante;
    private Long pacientesComMultiplosAcompanhantes;
    private Long maxAcompanhantesPorPaciente;
    
    // ===== DISTRIBUIÇÃO POR PARENTESCO =====
    private Map<String, Long> acompanhantesPorParentesco;
    private ParentescoEstatisticaDTO parentescoMaisComum;
    
    // ===== DADOS CLÍNICOS =====
    private Long pacientesComDadosClinicos;
    private Long pacientesSemDadosClinicos;
    private Long pacientesComSonda;
    private Long pacientesComCurativo;
    private Map<String, Long> pacientesPorTipoSonda;
    
    // ===== INFORMAÇÕES HOSPITALARES =====
    private Long pacientesComInformacaoHospitalar;
    private Long pacientesSemInformacaoHospitalar;
    
    // ===== ACOMPANHANTES QUE PODEM AJUDAR NA COZINHA =====
    private Long acompanhantesPodemAjudarCozinha;
    private Long acompanhantesNaoPodemAjudarCozinha;
    private Double percentualAjudamCozinha;
    
    // ===== CONTATOS DE EMERGÊNCIA =====
    private Long pacientesComContatoEmergencia;
    private Long pacientesSemContatoEmergencia;
    private Double mediaContatosEmergenciaPorPaciente;
    
    // ===== DISTRIBUIÇÃO GEOGRÁFICA =====
    private Map<String, Long> pacientesPorEstado;
    private Map<String, Long> pacientesPorCidade;
    private Map<String, Long> acompanhantesPorEstado;
    private Map<String, Long> acompanhantesPorCidade;
    
    // ===== TOP LISTAS =====
    private List<CidadeEstatisticaDTO> topCidadesComMaisPacientes;
    private List<CidadeEstatisticaDTO> topCidadesComMaisAcompanhantes;
    
    // ===== TAXAS E PERCENTUAIS =====
    private Double taxaPacientesAtivos;
    private Double taxaAcompanhantesAtivos;
    private Double taxaPacientesComAcompanhante;
    private Double taxaPacientesComDadosClinicos;
    
    // ===== TENDÊNCIAS TEMPORAIS =====
    private List<RegistroMensalDTO> registrosPacientesPorMes;
    private List<RegistroMensalDTO> registrosAcompanhantesPorMes;
    
    // ===== METADADOS =====
    private LocalDateTime dataHoraConsulta;
    private String periodoAnalisado;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentescoEstatisticaDTO {
        private String parentesco;
        private Long quantidade;
        private Double percentualTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CidadeEstatisticaDTO {
        private String cidade;
        private String estado;
        private Long totalPacientes;
        private Long totalAcompanhantes;
        private Double percentualTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistroMensalDTO {
        private Integer ano;
        private Integer mes;
        private String mesNome;
        private Long totalRegistros;
    }
}
