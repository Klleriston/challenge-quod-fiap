package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.application.dto.FraudAnalysisResultDTO;
import com.fiap.challengefiapquod.application.dto.ImageAnalysisResultDTO;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class FraudDetectionService {

    private final ImageDescriptionService imageDescriptionService;
    private final NotificationService notificationService;

    public FraudDetectionService(ImageDescriptionService imageDescriptionService,
                                 NotificationService notificationService) {
        this.imageDescriptionService = imageDescriptionService;
        this.notificationService = notificationService;
    }

    public FraudAnalysisResultDTO analyzeImage(String imageUrl, String userId) {
        try {
            ImageAnalysisResultDTO imageAnalysis = imageDescriptionService.analyzeImageFromUrl(imageUrl);

            boolean isFraud = detectPossibleFraud(imageAnalysis);
            String fraudReason = isFraud ? determineFraudReason(imageAnalysis) : null;

            if (isFraud) {
                notificationService.notifyFraudDetection(userId, imageUrl, fraudReason);
            } else {
                notificationService.logSuccessfulAnalysis(userId, imageUrl, imageAnalysis.getDescription());
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
            result.setAnalysisId(generateAnalysisId(userId, imageUrl));
            result.setFaces(imageAnalysis.getFaces());

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao analisar imagem para fraude: " + e.getMessage(), e);
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
}