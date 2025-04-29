package com.fiap.challengefiapquod.presentation.controller;

import com.fiap.challengefiapquod.application.dto.FingerprintRequestDTO;
import com.fiap.challengefiapquod.application.dto.FingerprintResponseDTO;
import com.fiap.challengefiapquod.domain.model.User;
import com.fiap.challengefiapquod.domain.service.FingerprintService;
import com.fiap.challengefiapquod.domain.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/secure/fingerprint")
public class FingerprintController {

    private final FingerprintService fingerprintService;
    private final UserService userService;

    public FingerprintController(
            FingerprintService fingerprintService,
            UserService userService) {
        this.fingerprintService = fingerprintService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<FingerprintResponseDTO> registerFingerprint(
            @RequestBody FingerprintRequestDTO request,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);

            if (user == null) {
                throw new IllegalStateException("Usuário não encontrado");
            }

            // Primeiro faz download e verifica se é uma digital válida
            byte[] imageData;
            try {
                imageData = fingerprintService.downloadImage(request.getImageUrl());
            } catch (Exception e) {
                return ResponseEntity.status(400).body(new FingerprintResponseDTO(
                        "Falha ao acessar a imagem: " + e.getMessage(),
                        true
                ));
            }

            // Verifica se é uma impressão digital válida
            if (!fingerprintService.isValidFingerprintImage(imageData)) {
                return ResponseEntity.status(400).body(new FingerprintResponseDTO(
                        "A imagem fornecida não é uma impressão digital válida",
                        true
                ));
            }

            // Se passou na validação, prossegue com o registro
            fingerprintService.registerFingerprint(user.getId(), request.getImageUrl());

            FingerprintResponseDTO response = new FingerprintResponseDTO(
                    "Biometria digital cadastrada com sucesso",
                    false
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Para outros erros internos
            return ResponseEntity.status(500).body(new FingerprintResponseDTO(
                    "Falha ao cadastrar biometria digital: " + e.getMessage(),
                    true
            ));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<FingerprintResponseDTO> verifyFingerprint(
            @RequestBody FingerprintRequestDTO request,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);

            if (user == null) {
                throw new IllegalStateException("Usuário não encontrado");
            }

            boolean isValid = fingerprintService.verifyFingerprint(user.getId(), request.getImageUrl());

            FingerprintResponseDTO response;
            if (isValid) {
                response = new FingerprintResponseDTO(
                        "Verificação biométrica realizada com sucesso",
                        false
                );
            } else {
                response = new FingerprintResponseDTO(
                        "Falha na verificação biométrica",
                        true
                );
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            FingerprintResponseDTO response = new FingerprintResponseDTO(
                    "Erro durante verificação biométrica: " + e.getMessage(),
                    true
            );

            return ResponseEntity.badRequest().body(response);
        }
    }
}