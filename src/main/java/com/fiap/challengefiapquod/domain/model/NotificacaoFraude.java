package com.fiap.challengefiapquod.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificacoes_fraude")
public class NotificacaoFraude {
    @Id
    private String transacaoId;
    private String tipoBiometria;
    private String tipoFraude;
    private LocalDateTime dataCaptura;

    private DispositivoInfo dispositivo;
    private List<String> canalNotificacao;
    private String notificadoPor;
    private Map<String, Object> metadados;
    private boolean processada;
    private LocalDateTime dataCriacao;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispositivoInfo {
        private String fabricante;
        private String modelo;
        private String sistemaOperacional;
    }
}