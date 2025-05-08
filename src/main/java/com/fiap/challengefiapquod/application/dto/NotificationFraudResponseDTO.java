package com.fiap.challengefiapquod.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFraudResponseDTO {
    private String transacaoId;
    private boolean sucesso;
    private String mensagem;
}