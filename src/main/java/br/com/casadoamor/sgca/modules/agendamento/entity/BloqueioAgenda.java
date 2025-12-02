package br.com.casadoamor.sgca.modules.agendamento.entity;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa bloqueios de agenda dos profissionais (férias, reuniões, etc)
 */
@Entity
@Table(name = "bloqueios_agenda")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloqueioAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_usuario_id", nullable = false)
    private AuthUsuario profissionalUsuario;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "tipo")
    private String tipo; // FERIAS, REUNIAO, TREINAMENTO, etc

    @Column(name = "ativo")
    private Boolean ativo;
}
