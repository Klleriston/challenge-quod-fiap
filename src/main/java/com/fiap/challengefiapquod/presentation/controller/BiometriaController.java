package com.fiap.challengefiapquod.presentation.controller;

import com.fiap.challengefiapquod.application.dto.*;
import com.fiap.challengefiapquod.domain.model.User;
import com.fiap.challengefiapquod.domain.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/biometria")
@RequiredArgsConstructor
public class BiometriaController {

    private final BiometriaDigitalService biometriaDigitalService;
    private final UserService userService;
    private final FraudDetectionService fraudDetectionService;

    @PostMapping("/facial")
    public ResponseEntity<BiometriaResponseDTO> validarBiometriaFacial(
            @RequestBody BiometriaRequestDTO request,
            Authentication authentication) {
        String userId = getUserId(authentication);
        
        FraudAnalysisResultDTO fraudResult;
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            fraudResult = fraudDetectionService.analyzeImage(request.getImageUrl(), userId);
        } else if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
            fraudResult = fraudDetectionService.analyzeImageBase64(request.getImageBase64(), userId);
        } else {
            return ResponseEntity.badRequest().body(
                BiometriaResponseDTO.builder()
                    .transacaoId("ERRO")
                    .valido(false)
                    .mensagem("Requisição inválida: é necessário fornecer imageUrl ou imageBase64")
                    .build()
            );
        }

        BiometriaResponseDTO response = BiometriaResponseDTO.builder()
                .transacaoId(fraudResult.getAnalysisId())
                .valido(!fraudResult.isFraud())
                .mensagem(fraudResult.isFraud() ? "Validação facial falhou" : "Biometria facial validada com sucesso")
                .statusDetalhado(fraudResult.getImageDescription())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/digital")
    public ResponseEntity<BiometriaResponseDTO> validarBiometriaDigital(
            @RequestBody BiometriaRequestDTO request,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(biometriaDigitalService.validarBiometria(request, userId));
    }

    private String getUserId(Authentication authentication) {
        if (authentication == null) {
            return "anonymous";
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        if (user == null) {
            throw new IllegalStateException("Usuário não encontrado");
        }

        return user.getId();
    }
}