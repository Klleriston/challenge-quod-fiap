package com.fiap.challengefiapquod.presentation.controller;

import com.fiap.challengefiapquod.application.dto.ImageAnalysisResultDTO;
import com.fiap.challengefiapquod.application.dto.ImageUrlDTO;
import com.fiap.challengefiapquod.domain.service.ImageDescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images/description")
public class ImageDescriptionController {

    private final ImageDescriptionService imageDescriptionService;

    public ImageDescriptionController(ImageDescriptionService imageDescriptionService) {
        this.imageDescriptionService = imageDescriptionService;
    }

    @PostMapping
    public ResponseEntity<ImageAnalysisResultDTO> describeImage(@RequestBody ImageUrlDTO imageUrlDTO) {
        if (imageUrlDTO.getImageURL() == null || imageUrlDTO.getImageURL().trim().isEmpty()) {
            throw new IllegalArgumentException("A URL da imagem não pode estar vazia");
        }

        try {
            ImageAnalysisResultDTO result = imageDescriptionService.analyzeImageFromUrl(imageUrlDTO.getImageURL());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar a imagem: " + e.getMessage(), e);
        }
    }
}