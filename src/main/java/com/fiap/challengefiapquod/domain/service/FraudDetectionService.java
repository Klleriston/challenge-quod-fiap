package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.application.dto.FraudAnalysisResultDTO;
import com.fiap.challengefiapquod.application.dto.ImageAnalysisResultDTO;
import com.fiap.challengefiapquod.domain.model.NotificacaoFraude;
import com.fiap.challengefiapquod.domain.repository.NotificationFraudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final ImageDescriptionService imageDescriptionService;
    private final NotificationFraudRepository notificacaoFraudeRepository;

    public FraudAnalysisResultDTO analyzeImage(String imageUrl, String userId) {
        try {
            ImageAnalysisResultDTO imageAnalysis = imageDescriptionService.analyzeImageFromUrl(imageUrl);

            boolean isFraud = detectPossibleFraud(imageAnalysis);
            String fraudReason = isFraud ? determineFraudReason(imageAnalysis) : null;
            String tipoFraude = convertToTipoFraude(fraudReason);

            String analysisId = generateAnalysisId(userId, imageUrl);

            if (isFraud) {
                NotificacaoFraude notificacao = criarNotificacaoFraude(
                        analysisId,
                        "facial",
                        tipoFraude,
                        imageUrl
                );

                notificacaoFraudeRepository.save(notificacao);
            }

            FraudAnalysisResultDTO result = new FraudAnalysisResultDTO();
            result.setImageDescription(imageAnalysis.getDescription());
            result.setContainsFaces(imageAnalysis.isContainsFaces());
            result.setFaceCount(imageAnalysis.getFaceCount());
            result.setFraud(isFraud);
            result.setFraudReason(fraudReason);
            result.setImageWidth(imageAnalysis.getImageWidth());
            result.setImageHeight(imageAnalysis.getImageHeight());
            result.setImageType(imageAnalysis.getImageType());
            result.setAnalysisId(analysisId);
            result.setFaces(imageAnalysis.getFaces());

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao analisar imagem para fraude: " + e.getMessage(), e);
        }
    }

    private String convertToTipoFraude(String fraudReason) {
        if (fraudReason == null) return null;

        if (fraudReason.contains("Nenhum rosto")) {
            return "sem_face";
        } else if (fraudReason.contains("Múltiplos rostos")) {
            return "multiplas_faces";
        } else if (fraudReason.contains("rosto frontal")) {
            return "face_nao_frontal";
        } else if (fraudReason.contains("resolução")) {
            return "baixa_qualidade";
        } else {
            return "deepfake";
        }
    }

    private boolean detectPossibleFraud(ImageAnalysisResultDTO imageAnalysis) {
        if (!imageAnalysis.isContainsFaces()) {
            return true;
        }

        if (imageAnalysis.getFaceCount() > 1) {
            return true;
        }

        boolean hasFrontalFace = false;
        for (var face : imageAnalysis.getFaces()) {
            if (face.isFrontal() && face.isHasEyes()) {
                hasFrontalFace = true;
                break;
            }
        }

        if (!hasFrontalFace) {
            return true;
        }

        if (imageAnalysis.getImageWidth() < 200 || imageAnalysis.getImageHeight() < 200) {
            return true;
        }

        return false;
    }

    private String determineFraudReason(ImageAnalysisResultDTO imageAnalysis) {
        if (!imageAnalysis.isContainsFaces()) {
            return "Nenhum rosto humano detectado na imagem";
        }

        if (imageAnalysis.getFaceCount() > 1) {
            return "Múltiplos rostos detectados quando se espera apenas um";
        }

        boolean hasFrontalFace = false;
        for (var face : imageAnalysis.getFaces()) {
            if (face.isFrontal() && face.isHasEyes()) {
                hasFrontalFace = true;
                break;
            }
        }

        if (!hasFrontalFace) {
            return "Não foi detectado um rosto frontal com olhos visíveis";
        }

        if (imageAnalysis.getImageWidth() < 200 || imageAnalysis.getImageHeight() < 200) {
            return "Imagem com resolução muito baixa";
        }

        return "Sinais de manipulação ou fraude detectados";
    }

    private String generateAnalysisId(String userId, String imageUrl) {
        String baseString = userId + ":" + imageUrl + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(baseString.getBytes()).substring(0, 20);
    }

    private NotificacaoFraude criarNotificacaoFraude(
            String transacaoId,
            String tipoBiometria,
            String tipoFraude,
            String imageUrl) {

        NotificacaoFraude.DispositivoInfo dispositivo = NotificacaoFraude.DispositivoInfo.builder()
                .fabricante("Desconhecido")
                .modelo("Desconhecido")
                .sistemaOperacional("Desconhecido")
                .build();

        Map<String, Object> metadados = new HashMap<>();
        metadados.put("latitude", -23.55052);
        metadados.put("longitude", -46.633308);
        metadados.put("ipOrigem", "127.0.0.1");
        metadados.put("imageUrl", imageUrl);

        return NotificacaoFraude.builder()
                .transacaoId(transacaoId)
                .tipoBiometria(tipoBiometria)
                .tipoFraude(tipoFraude)
                .dataCaptura(LocalDateTime.now())
                .dispositivo(dispositivo)
                .canalNotificacao(Arrays.asList("sms", "email"))
                .notificadoPor("sistema-analise-fraude")
                .metadados(metadados)
                .processada(false)
                .dataCriacao(LocalDateTime.now())
                .build();
    }
}