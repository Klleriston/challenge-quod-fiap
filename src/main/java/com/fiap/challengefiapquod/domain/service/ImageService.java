package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.domain.model.Image;
import com.fiap.challengefiapquod.domain.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageAnalysisService imageAnalysisService;

    public ImageService(ImageRepository imageRepository, ImageAnalysisService imageAnalysisService) {
        this.imageRepository = imageRepository;
        this.imageAnalysisService = imageAnalysisService;
    }


    public Image saveImage(MultipartFile file, String userId) throws IOException {
        Image analyzedImage = imageAnalysisService.analyzeImage(file, userId);
        return imageRepository.save(analyzedImage);
    }


    public Image getImage(String id) {
        return imageRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Image not found with id: " + id));
    }

    public List<Image> getUserImages(String userId) {
        return imageRepository.findByUserId(userId);
    }

    public void deleteImage(String id) {
        imageRepository.deleteById(id);
    }

    public boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return false;
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return false;
        }

        return true;
    }
}