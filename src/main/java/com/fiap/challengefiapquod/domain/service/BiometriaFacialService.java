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
            ImageAnalysisResultDTO imageAnalysis;
            
            if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
                imageAnalysis = imageDescriptionService.analyzeImageFromUrl(request.getImageUrl());
            } else if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
                imageAnalysis = imageDescriptionService.analyzeImageFromBase64(request.getImageBase64());
            } else {
                return BiometriaResponseDTO.builder()
                    .transacaoId(transactionId)
                    .valido(false)
                    .mensagem("Requisição inválida")
                    .statusDetalhado("É necessário fornecer imageUrl ou imageBase64")
                    .build();
            }
            
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

    private String convertNotificacaoToJson(NotificacaoFraude notification) {
        try {
            if (notification == null) {
                return "Notificação não encontrada";
            }

            return notification.getTransacaoId() + " - "
                    + notification.getTipoBiometria() + " - "
                    + notification.getTipoFraude() + " - "
                    + notification.getDataCaptura().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return "Erro ao converter notificação: " + e.getMessage();
        }
    }
}