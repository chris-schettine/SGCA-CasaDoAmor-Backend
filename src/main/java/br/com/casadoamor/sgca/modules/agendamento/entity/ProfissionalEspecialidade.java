package br.com.casadoamor.sgca.modules.agendamento.entity;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.EspecialidadeProfissional;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que vincula profissionais de saúde (auth_usuarios) com suas especialidades
 */
@Entity
@Table(name = "profissionais_especialidades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalEspecialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_usuario_id", nullable = false)
    private AuthUsuario profissionalUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_servico", nullable = false)
    private EspecialidadeProfissional categoriaServico;

    @Column(name = "numero_registro")
    private String numeroRegistro;

    @Column(name = "ativo")
    private Boolean ativo;
}
