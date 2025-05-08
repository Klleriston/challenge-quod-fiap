package com.fiap.challengefiapquod.domain.service;

import lombok.Getter;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Getter
@Service
public class ImageAnalysisService {

    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private CascadeClassifier profileFaceCascade;

    @PostConstruct
    public void init() throws IOException {
        nu.pattern.OpenCV.loadLocally();
        faceCascade = loadClassifier("haarcascade_frontalface_default.xml");
        eyesCascade = loadClassifier("haarcascade_eye.xml");

        try {
            profileFaceCascade = loadClassifier("haarcascade_profileface.xml");
        } catch (IOException e) {
            profileFaceCascade = null;
        }

    }

    private CascadeClassifier loadClassifier(String classifierName) throws IOException {
        Path tempDir = Files.createTempDirectory("opencv");
        File classifierFile = new File(tempDir.toFile(), classifierName);

        try (InputStream is = getClass().getResourceAsStream("/opencv/" + classifierName)) {
            if (is == null) {
                throw new IOException("Classificador não encontrado: " + classifierName);
            }
            Files.copy(is, classifierFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        CascadeClassifier classifier = new CascadeClassifier();
        if (!classifier.load(classifierFile.getAbsolutePath())) {
            throw new IOException("Não foi possível carregar o classificador: " + classifierName);
        }

        return classifier;
    }}


