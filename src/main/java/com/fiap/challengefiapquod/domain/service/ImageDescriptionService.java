package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.application.dto.FaceDescriptionDTO;
import com.fiap.challengefiapquod.application.dto.ImageAnalysisResultDTO;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ImageDescriptionService {

    private final CascadeClassifier faceCascade;
    private final CascadeClassifier eyesCascade;
    private final CascadeClassifier profileFaceCascade;

    public ImageDescriptionService(ImageAnalysisService imageAnalysisService) {
        this.faceCascade = imageAnalysisService.getFaceCascade();
        this.eyesCascade = imageAnalysisService.getEyesCascade();
        this.profileFaceCascade = imageAnalysisService.getProfileFaceCascade();
    }

    public ImageAnalysisResultDTO analyzeImageFromUrl(String imageUrl) {
        try {
            byte[] imageData = downloadImage(imageUrl);
            return analyzeImageData(imageData, determineImageType(imageUrl));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao analisar imagem da URL: " + e.getMessage(), e);
        }
    }

    public ImageAnalysisResultDTO analyzeImageFromBase64(String base64Data) {
        try {
            String cleanedBase64 = base64Data;
            if (base64Data.contains(",")) {
                cleanedBase64 = base64Data.split(",")[1];
            }
            
            byte[] imageData = Base64.getDecoder().decode(cleanedBase64);

            String imageType = "Desconhecido";
            if (base64Data.contains("data:image/")) {
                String metadata = base64Data.split(",")[0];
                if (metadata.contains("jpeg") || metadata.contains("jpg")) {
                    imageType = "JPEG";
                } else if (metadata.contains("png")) {
                    imageType = "PNG";
                } else if (metadata.contains("gif")) {
                    imageType = "GIF";
                } else if (metadata.contains("bmp")) {
                    imageType = "BMP";
                } else if (metadata.contains("webp")) {
                    imageType = "WEBP";
                }
            }
            
            return analyzeImageData(imageData, imageType);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao analisar imagem Base64: " + e.getMessage(), e);
        }
    }
    
    private ImageAnalysisResultDTO analyzeImageData(byte[] imageData, String imageType) {
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);

        if (image.empty()) {
            throw new IllegalArgumentException("Não foi possível carregar a imagem");
        }

        ImageAnalysisResultDTO result = new ImageAnalysisResultDTO();

        result.setImageWidth(image.cols());
        result.setImageHeight(image.rows());
        result.setImageType(imageType);

        List<FaceDescriptionDTO> faceDescriptions = detectFaces(image);
        result.setFaces(faceDescriptions);
        result.setFaceCount(faceDescriptions.size());
        result.setContainsFaces(!faceDescriptions.isEmpty());

        result.setDescription(generateImageDescription(image, faceDescriptions));

        return result;
    }

    private byte[] downloadImage(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        try (InputStream inputStream = connection.getInputStream()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }
    }

    private String determineImageType(String imageUrl) {
        String lowercaseUrl = imageUrl.toLowerCase();
        if (lowercaseUrl.endsWith(".jpg") || lowercaseUrl.endsWith(".jpeg")) {
            return "JPEG";
        } else if (lowercaseUrl.endsWith(".png")) {
            return "PNG";
        } else if (lowercaseUrl.endsWith(".gif")) {
            return "GIF";
        } else if (lowercaseUrl.endsWith(".bmp")) {
            return "BMP";
        } else if (lowercaseUrl.endsWith(".webp")) {
            return "WEBP";
        } else {
            return "Desconhecido";
        }
    }

    private List<FaceDescriptionDTO> detectFaces(Mat image) {
        List<FaceDescriptionDTO> faces = new ArrayList<>();

        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayImage, grayImage);

        MatOfRect frontalFaces = new MatOfRect();
        faceCascade.detectMultiScale(grayImage, frontalFaces, 1.1, 3, 0, new Size(30, 30), new Size());

        MatOfRect profileFaces = new MatOfRect();
        if (profileFaceCascade != null) {
            profileFaceCascade.detectMultiScale(grayImage, profileFaces, 1.1, 3, 0, new Size(30, 30), new Size());
        }

        for (Rect faceRect : frontalFaces.toArray()) {
            boolean hasEyes = detectEyes(grayImage.submat(faceRect));

            faces.add(new FaceDescriptionDTO(
                    faceRect.x,
                    faceRect.y,
                    faceRect.width,
                    faceRect.height,
                    hasEyes,
                    true
            ));
        }

        for (Rect faceRect : profileFaces.toArray()) {
            boolean isOverlapping = false;
            for (FaceDescriptionDTO existingFace : faces) {
                if (isRectangleOverlap(faceRect,
                        new Rect(existingFace.getX(), existingFace.getY(),
                                existingFace.getWidth(), existingFace.getHeight()))) {
                    isOverlapping = true;
                    break;
                }
            }

            if (!isOverlapping) {
                faces.add(new FaceDescriptionDTO(
                        faceRect.x,
                        faceRect.y,
                        faceRect.width,
                        faceRect.height,
                        false,
                        false
                ));
            }
        }

        return faces;
    }

    private boolean detectEyes(Mat faceRegion) {
        MatOfRect eyes = new MatOfRect();
        eyesCascade.detectMultiScale(faceRegion, eyes);
        return eyes.toArray().length >= 2;
    }

    private boolean isRectangleOverlap(Rect r1, Rect r2) {
        return !(r1.x > r2.x + r2.width ||
                r1.x + r1.width < r2.x ||
                r1.y > r2.y + r2.height ||
                r1.y + r1.height < r2.y);
    }

    private String generateImageDescription(Mat image, List<FaceDescriptionDTO> faces) {
        StringBuilder description = new StringBuilder();

        description.append("Imagem de ")
                .append(image.cols())
                .append("x")
                .append(image.rows())
                .append(" pixels. ");

        if (faces.isEmpty()) {
            description.append("Não foram detectados rostos humanos nesta imagem.");
        } else {
            description.append("Foram detectados ")
                    .append(faces.size())
                    .append(faces.size() == 1 ? " rosto " : " rostos ")
                    .append("na imagem. ");

            long frontalCount = faces.stream().filter(FaceDescriptionDTO::isFrontal).count();
            long profileCount = faces.size() - frontalCount;

            if (frontalCount > 0) {
                description.append(frontalCount)
                        .append(frontalCount == 1 ? " face frontal" : " faces frontais");

                if (profileCount > 0) {
                    description.append(" e ")
                            .append(profileCount)
                            .append(profileCount == 1 ? " face de perfil" : " faces de perfil");
                }
                description.append(". ");
            } else if (profileCount > 0) {
                description.append(profileCount)
                        .append(profileCount == 1 ? " face de perfil. " : " faces de perfil. ");
            }

            int facesWithEyes = 0;
            for (FaceDescriptionDTO face : faces) {
                if (face.isHasEyes()) {
                    facesWithEyes++;
                }
            }

            if (facesWithEyes > 0) {
                description.append("Em ")
                        .append(facesWithEyes)
                        .append(facesWithEyes == 1 ? " face " : " faces ")
                        .append("é possível identificar claramente os olhos. ");
            }
        }

        if (faces.size() == 1 && faces.get(0).isFrontal()) {
            FaceDescriptionDTO face = faces.get(0);
            double faceArea = face.getWidth() * face.getHeight();
            double imageArea = image.cols() * image.rows();
            double faceRatio = faceArea / imageArea;

            Point faceCenter = new Point(
                    face.getX() + face.getWidth() / 2.0,
                    face.getY() + face.getHeight() / 2.0
            );

            Point imageCenter = new Point(
                    image.cols() / 2.0,
                    image.rows() / 2.0
            );

            double distanceToCenter = Math.sqrt(
                    Math.pow(faceCenter.x - imageCenter.x, 2) +
                            Math.pow(faceCenter.y - imageCenter.y, 2)
            );

            double imageDiagonal = Math.sqrt(
                    Math.pow(image.cols(), 2) +
                            Math.pow(image.rows(), 2)
            );

            double relativeDistance = distanceToCenter / imageDiagonal;

            if (faceRatio > 0.1 && relativeDistance < 0.25 && face.isHasEyes()) {
                description.append("Esta imagem parece ser uma selfie, com o rosto centralizado e ocupando parte significativa da foto.");
            }
        } else if (faces.size() > 1) {
            description.append("Esta imagem parece ser uma foto de grupo com múltiplas pessoas.");
        }

        return description.toString();
    }
}