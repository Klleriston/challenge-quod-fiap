package com.fiap.challengefiapquod.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometriaResponseDTO {
    private String transacaoId;
    private boolean valido;
    private String mensagem;
    private String statusDetalhado;
}