package com.fiap.challengefiapquod.presentation.controller;

import com.fiap.challengefiapquod.application.dto.DocumentRequestDTO;
import com.fiap.challengefiapquod.application.dto.DocumentResponseDTO;
import com.fiap.challengefiapquod.domain.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @PostMapping("/validar")
    public ResponseEntity<DocumentResponseDTO> validarDocumento(@RequestBody DocumentRequestDTO request) {
        try {
            DocumentResponseDTO response = documentoService.validarDocumento(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            return ResponseEntity.ok(DocumentResponseDTO.builder()
                    .status("FRAUDE")
                    .mensagem("Documento inválido detectado. Uma notificação foi enviada para o sistema de monitoramento.")
                    .build());
        }
    }
}