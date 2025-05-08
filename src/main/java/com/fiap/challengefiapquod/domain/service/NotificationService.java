package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.domain.model.NotificacaoFraude;
import com.fiap.challengefiapquod.domain.repository.NotificationFraudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationFraudRepository notificationFraudRepository;

    public String notificarFraude(String tipoBiometria, String tipoFraude, Map<String, Object> metadados) {
        try {
            String transactionId = UUID.randomUUID().toString();

            NotificacaoFraude.DispositivoInfo dispositivo = NotificacaoFraude.DispositivoInfo.builder()
                    .fabricante("Desconhecido")
                    .modelo("Desconhecido")
                    .sistemaOperacional("Desconhecido")
                    .build();

            List<String> channelNotification = Arrays.asList("email", "sms");

            NotificacaoFraude notification = NotificacaoFraude.builder()
                    .transacaoId(transactionId)
                    .tipoBiometria(tipoBiometria)
                    .tipoFraude(tipoFraude)
                    .dataCaptura(LocalDateTime.now())
                    .dispositivo(dispositivo)
                    .canalNotificacao(channelNotification)
                    .notificadoPor("sistema-biometria")
                    .metadados(metadados != null ? metadados : createDefaultMetadata())
                    .processada(false)
                    .dataCriacao(LocalDateTime.now())
                    .build();

            notificationFraudRepository.save(notification);

            System.out.println("FRAUDE DETECTADA: " + tipoFraude + " do tipo " + tipoBiometria
                    + ", transação: " + transactionId);

            return transactionId;
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação de fraude: " + e.getMessage());
            return null;
        }
    }

    public void registrarProcessamentoSucesso(String tipoBiometria, String referencia) {
        System.out.println("PROCESSAMENTO BEM-SUCEDIDO: " + tipoBiometria + " - " + referencia);
    }

    private Map<String, Object> createDefaultMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("latitude", -23.55052);
        metadata.put("longitude", -46.633308);
        metadata.put("ipOrigem", "127.0.0.1");
        return metadata;
    }
}