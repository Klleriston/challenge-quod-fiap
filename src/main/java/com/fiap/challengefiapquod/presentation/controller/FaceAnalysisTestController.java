package com.fiap.challengefiapquod.presentation.controller;

import com.fiap.challengefiapquod.domain.model.Image;
import com.fiap.challengefiapquod.domain.service.ImageAnalysisService;
import com.fiap.challengefiapquod.domain.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class FaceAnalysisTestController {

    private final ImageAnalysisService imageAnalysisService;
    private final ImageService imageService;

    public FaceAnalysisTestController(ImageAnalysisService imageAnalysisService,
                                      ImageService imageService) {
        this.imageAnalysisService = imageAnalysisService;
        this.imageService = imageService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeImage(
            @RequestParam("image") MultipartFile file) throws IOException {

        Image result = imageAnalysisService.analyzeImage(file, "demo-user");

        Map<String, Object> response = new HashMap<>();
        response.put("isSelfie", result.isSelfie());
        response.put("isVerified", result.isVerified());
        response.put("verificationStatus", result.getVerificationStatus());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadTestImage(
            @RequestParam("image") MultipartFile file) throws IOException {

        Image savedImage = imageService.saveImage(file, "demo-user");

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedImage.getId());
        response.put("isSelfie", savedImage.isSelfie());
        response.put("isVerified", savedImage.isVerified());
        response.put("verificationStatus", savedImage.getVerificationStatus());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getTestImage(@PathVariable String id) {
        Image image = imageService.getImage(id);

        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf(image.getContentType()))
                .body(image.getData());
    }
}