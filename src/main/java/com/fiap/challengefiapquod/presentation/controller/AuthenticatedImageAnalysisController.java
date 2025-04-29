package com.fiap.challengefiapquod.presentation.controller;

import com.fiap.challengefiapquod.application.dto.FraudAnalysisResultDTO;
import com.fiap.challengefiapquod.application.dto.ImageUrlDTO;
import com.fiap.challengefiapquod.domain.model.AnalysisRecord;
import com.fiap.challengefiapquod.domain.model.User;
import com.fiap.challengefiapquod.domain.service.AnalysisStorageService;
import com.fiap.challengefiapquod.domain.service.FraudDetectionService;
import com.fiap.challengefiapquod.domain.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secure/image-analysis")
public class AuthenticatedImageAnalysisController {

    private final FraudDetectionService fraudDetectionService;
    private final UserService userService;
    private final AnalysisStorageService analysisStorageService;

    public AuthenticatedImageAnalysisController(FraudDetectionService fraudDetectionService,
                                                UserService userService,
                                                AnalysisStorageService analysisStorageService) {
        this.fraudDetectionService = fraudDetectionService;
        this.userService = userService;
        this.analysisStorageService = analysisStorageService;
    }

    @PostMapping
    public ResponseEntity<FraudAnalysisResultDTO> analyzeImage(
            @RequestBody ImageUrlDTO imageUrlDTO,
            Authentication authentication) {

        if (imageUrlDTO.getImageURL() == null || imageUrlDTO.getImageURL().trim().isEmpty()) {
            throw new IllegalArgumentException("A URL da imagem não pode estar vazia");
        }

        String email = authentication.getName();

        User user = userService.findByEmail(email);
        if (user == null) {
            throw new IllegalStateException("Usuário não encontrado");
        }

        try {
            FraudAnalysisResultDTO result = fraudDetectionService.analyzeImage(
                    imageUrlDTO.getImageURL(),
                    user.getId()
            );

            analysisStorageService.storeAnalysisResult(user.getId(), imageUrlDTO.getImageURL(), result);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar a imagem: " + e.getMessage(), e);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisRecord>> getAnalysisHistory(Authentication authentication) {
        String email = authentication.getName();

        User user = userService.findByEmail(email);
        if (user == null) {
            throw new IllegalStateException("Usuário não encontrado");
        }

        List<AnalysisRecord> history = analysisStorageService.getUserAnalysisHistory(user.getId());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<AnalysisRecord> getAnalysisDetails(
            @PathVariable String analysisId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userService.findByEmail(email);
        if (user == null) {
            throw new IllegalStateException("Usuário não encontrado");
        }

        AnalysisRecord record = analysisStorageService.getAnalysisRecord(analysisId);

        if (!record.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(record);
    }
}