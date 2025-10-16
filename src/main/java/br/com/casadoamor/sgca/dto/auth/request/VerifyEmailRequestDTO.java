package br.com.casadoamor.sgca.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para verificação de email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailRequestDTO {

    @NotBlank(message = "Token de verificação é obrigatório")
    private String token;
}
