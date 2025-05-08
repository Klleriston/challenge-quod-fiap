package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.application.dto.BiometriaRequestDTO;
import com.fiap.challengefiapquod.application.dto.BiometriaResponseDTO;
import com.fiap.challengefiapquod.domain.model.NotificacaoFraude;
import com.fiap.challengefiapquod.domain.repository.NotificationFraudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BiometriaDigitalService implements BiometriaService {

    private final FingerprintService fingerprintService;
    private final NotificationService notificationService;
    private final NotificationFraudRepository notificationFraudeRepository;

    private static final String QUOD_API_URL = "https://api.quod.com.br/api/notificacoes/fraude";

    @Override
    public BiometriaResponseDTO validarBiometria(BiometriaRequestDTO request, String userId) {
        try {
            String transacaoId = UUID.randomUUID().toString();

            byte[] imageData = fingerprintService.downloadImage(request.getImageUrl());

            boolean isValid = fingerprintService.isValidFingerprintImage(imageData);

            if (!isValid) {
                Map<String, Object> metadados = criarMetadados(request);

                String notificationId = notificationService.notificarFraude("digital", "digital_falsa", metadados);

                NotificacaoFraude notification = notificationFraudeRepository.findByTransacaoId(notificationId);
                String jsonNotification = convertNotificacaoToJson(notification);

                return BiometriaResponseDTO.builder()
                        .transacaoId(transacaoId)
                        .valido(false)
                        .mensagem("Possível fraude detectada na biometria digital")
                        .statusDetalhado("Impressão digital inválida ou falsificada. " +
                                "Uma notificação foi enviada para " + QUOD_API_URL + " com os dados: " + jsonNotification)
                        .build();
            }

            return BiometriaResponseDTO.builder()
                    .transacaoId(transacaoId)
                    .valido(true)
                    .mensagem("Biometria digital validada com sucesso")
                    .statusDetalhado("Impressão digital válida")
                    .build();

        } catch (Exception e) {
            return BiometriaResponseDTO.builder()
                    .transacaoId(UUID.randomUUID().toString())
                    .valido(false)
                    .mensagem("Erro ao processar biometria digital")
                    .statusDetalhado("Erro interno: " + e.getMessage())
                    .build();
        }
    }


    private Map<String, Object> criarMetadados(BiometriaRequestDTO request) {
        return getStringObjectMap(request);
    }

    static Map<String, Object> getStringObjectMap(BiometriaRequestDTO request) {
        Map<String, Object> metadados = new HashMap<>();

        if (request.getLocalizacao() != null) {
            metadados.put("latitude", request.getLocalizacao().getLatitude());
            metadados.put("longitude", request.getLocalizacao().getLongitude());
            metadados.put("ipOrigem", request.getLocalizacao().getIpOrigem());
        } else {
            metadados.put("latitude", -23.55052);
            metadados.put("longitude", -46.633308);
            metadados.put("ipOrigem", "127.0.0.1");
        }

        metadados.put("imageUrl", request.getImageUrl());

        return metadados;
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
        if (map == null || map.isEmpty()) {
            return "{}";
        }

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