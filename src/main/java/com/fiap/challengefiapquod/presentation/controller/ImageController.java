package com.fiap.challengefiapquod.presentation.controller;

import com.fiap.challengefiapquod.application.dto.ImageDTO;
import com.fiap.challengefiapquod.domain.model.Image;
import com.fiap.challengefiapquod.domain.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ImageDTO> uploadImage(
            @RequestParam("image") MultipartFile file,
            Authentication authentication) throws IOException {

        if (!imageService.isValidImage(file)) {
            throw new IllegalArgumentException("Arquivo inválido. Por favor, envie uma imagem válida (JPG, PNG) de até 10MB.");
        }

        String email = authentication.getName();

        Image savedImage = imageService.saveImage(file, email);

        ImageDTO response = convertToDTO(savedImage);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ImageDTO>> getUserImages(Authentication authentication) {
        String email = authentication.getName();

        List<Image> images = imageService.getUserImages(email);
        List<ImageDTO> response = images.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        Image image = imageService.getImage(id);

        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf(image.getContentType()))
                .body(image.getData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable String id,
            Authentication authentication) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }

    private ImageDTO convertToDTO(Image image) {
        ImageDTO dto = new ImageDTO();
        dto.setId(image.getId());
        dto.setUserId(image.getUserId());
        dto.setFileName(image.getFileName());
        dto.setSelfie(image.isSelfie());
        dto.setVerified(image.isVerified());
        dto.setVerificationStatus(image.getVerificationStatus());
        return dto;
    }
}