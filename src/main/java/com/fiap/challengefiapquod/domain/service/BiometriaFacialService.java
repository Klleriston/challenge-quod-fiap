package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.application.dto.BiometriaRequestDTO;
import com.fiap.challengefiapquod.application.dto.BiometriaResponseDTO;
import com.fiap.challengefiapquod.application.dto.ImageAnalysisResultDTO;
import com.fiap.challengefiapquod.domain.model.NotificacaoFraude;
import com.fiap.challengefiapquod.domain.repository.NotificationFraudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.fiap.challengefiapquod.domain.service.BiometriaDigitalService.getStringObjectMap;

@Service
@RequiredArgsConstructor
public class BiometriaFacialService implements BiometriaService {

    private final ImageDescriptionService imageDescriptionService;
    private final NotificationService notificationService;
    private final NotificationFraudRepository notificationFraudRepository;

    private static final String QUOD_API_URL = "https://api.quod.com.br/api/notificacoes/fraude";

    @Override
    public BiometriaResponseDTO validarBiometria(BiometriaRequestDTO request, String userId) {
        try {
            String transactionId = UUID.randomUUID().toString();
            ImageAnalysisResultDTO imageAnalysis = imageDescriptionService.analyzeImageFromUrl(request.getImageUrl());
            boolean isValid = imageAnalysis.isContainsFaces() && !imageAnalysis.isFraud();

            if (!isValid) {
                Map<String, Object> metadados = criarMetadados(request);

                String fraudReason = imageAnalysis.getFraudReason() != null ?
                        imageAnalysis.getFraudReason() : "Imagem inválida";

                String notificationId = notificationService.notificarFraude("facial",
                        fraudReason, metadados);

                NotificacaoFraude notification = notificationFraudRepository.findByTransacaoId(notificationId);
                String jsonNotification = convertNotificacaoToJson(notification);

                return BiometriaResponseDTO.builder()
                        .transacaoId(transactionId)
                        .valido(false)
                        .mensagem("Validação facial falhou")
                        .statusDetalhado("Motivo: " + fraudReason + ". " +
                                "Uma notificação foi enviada para " + QUOD_API_URL +
                                " com os dados: " + jsonNotification)
                        .build();
            }

            return BiometriaResponseDTO.builder()
                    .transacaoId(transactionId)
                    .valido(true)
                    .mensagem("Biometria facial validada com sucesso")
                    .statusDetalhado("Descrição da imagem: " + imageAnalysis.getDescription())
                    .build();

        } catch (Exception e) {
            return BiometriaResponseDTO.builder()
                    .transacaoId(UUID.randomUUID().toString())
                    .valido(false)
                    .mensagem("Erro ao processar biometria facial")
                    .statusDetalhado("Erro interno: " + e.getMessage())
                    .build();
        }
    }

    private Map<String, Object> criarMetadados(BiometriaRequestDTO request) {
        return getStringObjectMap(request);
    }

    private String convertNotificacaoToJson(NotificacaoFraude notificacao) {
        if (notificacao == null) return "{}";

        String dataCaptura = notificacao.getDataCaptura().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        );

        StringBuilder json = new StringBuilder();
        json.append("{\n")
                .append("  \"transacaoId\": \"").append(notificacao.getTransacaoId()).append("\",\n")
                .append("  \"tipoBiometria\": \"").append(notificacao.getTipoBiometria()).append("\",\n")
                .append("  \"tipoFraude\": \"").append(notificacao.getTipoFraude()).append("\",\n")
                .append("  \"dataCaptura\": \"").append(dataCaptura).append("\",\n")
                .append("  \"dispositivo\": {\n")
                .append("    \"fabricante\": \"").append(notificacao.getDispositivo().getFabricante()).append("\",\n")
                .append("    \"modelo\": \"").append(notificacao.getDispositivo().getModelo()).append("\",\n")
                .append("    \"sistemaOperacional\": \"").append(notificacao.getDispositivo().getSistemaOperacional()).append("\"\n")
                .append("  },\n")
                .append("  \"canalNotificacao\": [\"").append(String.join("\", \"", notificacao.getCanalNotificacao())).append("\"],\n")
                .append("  \"notificadoPor\": \"").append(notificacao.getNotificadoPor()).append("\",\n")
                .append("  \"metadados\": ").append(convertMapToJson(notificacao.getMetadados())).append("\n")
                .append("}");

        return json.toString();
    }

    private String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;

            sb.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("\"").append(value).append("\"");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}