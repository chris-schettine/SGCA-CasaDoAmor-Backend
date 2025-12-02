package br.com.casadoamor.sgca.modules.agendamento.entity;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Entidade que define os horários de trabalho dos profissionais
 */
@Entity
@Table(name = "horarios_profissionais")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioProfissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_usuario_id", nullable = false)
    private AuthUsuario profissionalUsuario;

    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana; // 1=Segunda a 7=Domingo

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(name = "ativo")
    private Boolean ativo;
}
