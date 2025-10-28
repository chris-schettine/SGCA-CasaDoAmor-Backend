package br.com.casadoamor.sgca.entity.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Controle de rate limiting para envio de códigos 2FA
 * Previne abuso e spam de emails
 */
@Entity
@Table(name = "autenticacao_2fa_rate_limit",
        indexes = @Index(name = "idx_2fa_rate_limit_usuario", columnList = "usuario_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Autenticacao2FARateLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "ultimo_envio", nullable = false)
    private LocalDateTime ultimoEnvio;

    @Column(name = "tentativas_ultimos_15min", nullable = false)
    @Builder.Default
    private Integer tentativasUltimos15Min = 0;

    @Column(name = "tentativas_ultima_hora", nullable = false)
    @Builder.Default
    private Integer tentativasUltimaHora = 0;

    @Column(name = "tentativas_hoje", nullable = false)
    @Builder.Default
    private Integer tentativasHoje = 0;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    /**
     * Verifica se está bloqueado por excesso de tentativas
     */
    public boolean isBloqueado() {
        return bloqueadoAte != null && LocalDateTime.now().isBefore(bloqueadoAte);
    }

    /**
     * Reseta contadores se passou o período
     */
    public void resetarSeNecessario() {
        // Se nunca enviou código, não há nada para resetar
        if (ultimoEnvio == null) {
            return;
        }
        
        LocalDateTime agora = LocalDateTime.now();
        
        // Reseta contador de 15 minutos
        if (ultimoEnvio.plusMinutes(15).isBefore(agora)) {
            tentativasUltimos15Min = 0;
        }
        
        // Reseta contador de 1 hora
        if (ultimoEnvio.plusHours(1).isBefore(agora)) {
            tentativasUltimaHora = 0;
        }
        
        // Reseta contador diário
        if (ultimoEnvio.toLocalDate().isBefore(agora.toLocalDate())) {
            tentativasHoje = 0;
        }
    }

    /**
     * Incrementa contadores após envio
     */
    public void incrementarContadores() {
        tentativasUltimos15Min++;
        tentativasUltimaHora++;
        tentativasHoje++;
        ultimoEnvio = LocalDateTime.now();
    }

    /**
     * Verifica se pode enviar novo código
     * Limites:
     * - Máximo 3 códigos em 15 minutos
     * - Máximo 5 códigos por hora
     * - Máximo 10 códigos por dia
     */
    public boolean podeEnviarNovoCodigo() {
        resetarSeNecessario();
        
        if (isBloqueado()) {
            return false;
        }
        
        // Verifica limite de 15 minutos (3 códigos)
        if (tentativasUltimos15Min >= 3) {
            bloqueadoAte = LocalDateTime.now().plusMinutes(15);
            return false;
        }
        
        // Verifica limite de 1 hora (5 códigos)
        if (tentativasUltimaHora >= 5) {
            bloqueadoAte = LocalDateTime.now().plusHours(1);
            return false;
        }
        
        // Verifica limite diário (10 códigos)
        if (tentativasHoje >= 10) {
            bloqueadoAte = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
            return false;
        }
        
        // Deve esperar pelo menos 60 segundos entre envios
        if (ultimoEnvio != null && ultimoEnvio.plusSeconds(60).isAfter(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }

    /**
     * Retorna tempo de espera até próximo envio permitido
     */
    public String getTempoEsperaFormatado() {
        if (bloqueadoAte != null && bloqueadoAte.isAfter(LocalDateTime.now())) {
            long segundos = java.time.Duration.between(LocalDateTime.now(), bloqueadoAte).getSeconds();
            
            if (segundos < 60) {
                return segundos + " segundos";
            } else if (segundos < 3600) {
                return (segundos / 60) + " minutos";
            } else {
                return (segundos / 3600) + " horas";
            }
        }
        
        if (ultimoEnvio != null) {
            LocalDateTime proximoEnvio = ultimoEnvio.plusSeconds(60);
            if (proximoEnvio.isAfter(LocalDateTime.now())) {
                long segundos = java.time.Duration.between(LocalDateTime.now(), proximoEnvio).getSeconds();
                return segundos + " segundos";
            }
        }
        
        return "disponível agora";
    }
}
