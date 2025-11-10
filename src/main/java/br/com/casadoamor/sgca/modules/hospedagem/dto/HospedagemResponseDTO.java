package br.com.casadoamor.sgca.modules.hospedagem.dto;

import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO de resposta com dados completos da hospedagem
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospedagemResponseDTO {

    private String uuid;
    
    // Paciente
    private String pacienteId;
    private String pacienteNome;
    
    // Quarto
    private String quartoUuid;
    private String quartoNome;
    private String quartoCodigo;
    
    // Datas
    private LocalDate dataEntrada;
    private LocalTime horaEntrada;
    private LocalDate dataSaidaPrevista;
    private LocalDate dataSaida;
    private LocalTime horaSaida;
    
    // Status
    private StatusHospedagem status;
    private String motivoSaida;
    
    // Observações
    private String observacoesEntrada;
    private String observacoesSaida;
    private String observacoesGerais;
    
    // Auditoria
    private LocalDateTime createdAt;
    private String createdByNome;
    private LocalDateTime updatedAt;
    private String updatedByNome;
}
