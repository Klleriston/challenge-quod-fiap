package com.fiap.challengefiapquod.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean notifyFraudDetection(String userId, String imageUrl, String reason) {
        try {
            String notificationEndpoint = "https://api.fraudmonitoring.example/notify";

            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("userId", userId);
            notificationData.put("imageUrl", imageUrl);
            notificationData.put("detectionTime", System.currentTimeMillis());
            notificationData.put("reason", reason);
            notificationData.put("severity", "HIGH");

            System.out.println("FRAUDE DETECTADA: " + reason + " para usuário " + userId + ", imagem: " + imageUrl);

            return true;
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação de fraude: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registra análise bem-sucedida de imagem
     * @param userId ID do usuário
     * @param imageUrl URL da imagem
     * @param analysisResult Resultado da análise
     */
    public void logSuccessfulAnalysis(String userId, String imageUrl, String analysisResult) {
        // Em produção, poderia enviar para sistema de auditoria ou monitoramento
        System.out.println("ANÁLISE BEM-SUCEDIDA: Usuário " + userId + ", imagem: " + imageUrl);
    }
}