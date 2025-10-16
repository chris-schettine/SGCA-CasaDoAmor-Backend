package br.com.casadoamor.sgca.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico para respostas de sucesso
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {

    private String message;
    private Boolean success;

    public static MessageResponseDTO success(String message) {
        return MessageResponseDTO.builder()
                .message(message)
                .success(true)
                .build();
    }

    public static MessageResponseDTO error(String message) {
        return MessageResponseDTO.builder()
                .message(message)
                .success(false)
                .build();
    }
}
