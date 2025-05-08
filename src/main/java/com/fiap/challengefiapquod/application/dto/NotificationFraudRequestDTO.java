package com.fiap.challengefiapquod.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFraudRequestDTO {
    private String transacaoId;
    private String tipoBiometria;
    private String tipoFraude;
    private String dataCaptura;

    private DispositivoInfo dispositivo;
    private List<String> canalNotificacao;
    private String notificadoPor;
    private Metadados metadados;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispositivoInfo {
        private String fabricante;
        private String modelo;
        private String sistemaOperacional;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadados {
        private Double latitude;
        private Double longitude;
        private String ipOrigem;
    }
}