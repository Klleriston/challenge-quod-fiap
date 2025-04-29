package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.domain.model.FingerprintRecord;
import com.fiap.challengefiapquod.domain.repository.FingerprintRepository;
import jakarta.annotation.PostConstruct;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.*;

@Service
public class FingerprintService {

    private final FingerprintRepository fingerprintRepository;

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public FingerprintService(FingerprintRepository fingerprintRepository) {
        this.fingerprintRepository = fingerprintRepository;
    }

    @PostConstruct
    public void init() {
    }

    public FingerprintRecord registerFingerprint(String userId, String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("A URL da imagem não pode estar vazia");
            }

            boolean isValidFingerprint = imageUrl.toLowerCase().contains("fingerprint") ||
                    imageUrl.toLowerCase().contains("digital") ||
                    imageUrl.toLowerCase().contains("biometric");

            if (!isValidFingerprint) {
                throw new IllegalArgumentException("A imagem fornecida não parece ser uma impressão digital válida");
            }

            String mockHash = UUID.randomUUID().toString();

            FingerprintRecord record = new FingerprintRecord();
            record.setId(UUID.randomUUID().toString());
            record.setUserId(userId);
            record.setImageUrl(imageUrl);
            record.setFingerprintHash(mockHash);
            record.setRegistrationDate(new Date());

            return fingerprintRepository.save(record);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao registrar impressão digital: " + e.getMessage(), e);
        }
    }

    public boolean verifyFingerprint(String userId, String imageUrl) {
        try {
            if (fingerprintRepository.existsByImageUrl(imageUrl)) {
                return true;
            }

            byte[] imageData = downloadImage(imageUrl);

            if (!isValidFingerprintImage(imageData)) {
                return false;
            }

            String fingerprintHash = generateFingerprintHash(imageData);

            Optional<FingerprintRecord> record = fingerprintRepository.findByUserId(userId);

            if (record.isEmpty()) return false;

            return record.get().getFingerprintHash().equals(fingerprintHash);

        } catch (Exception e) {
            return false;
        }
    }

    public byte[] downloadImage(String imageUrl) throws Exception {
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

    public boolean isValidFingerprintImage(byte[] imageData) {
        try {
            Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_GRAYSCALE);

            if (image.empty()) {
                return false;
            }

            if (image.width() < 100 || image.height() < 100) {
                return false;
            }

           Mat processedImage = new Mat();
            Imgproc.equalizeHist(image, processedImage);

            Mat binaryImage = new Mat();
            Imgproc.threshold(processedImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

            int whitePixels = Core.countNonZero(binaryImage);
            int totalPixels = binaryImage.rows() * binaryImage.cols();
            int blackPixels = totalPixels - whitePixels;

            double blackRatio = (double) blackPixels / totalPixels;

            if (blackRatio < 0.2 || blackRatio > 0.7) {
                return false;
            }

            Mat edges = new Mat();
            Imgproc.Canny(binaryImage, edges, 50, 150);

            Mat lines = new Mat();
            Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 100);

            if (lines.rows() < 20) {
                return false;
            }

            double[] angles = new double[lines.rows()];
            for (int i = 0; i < lines.rows(); i++) {
                double[] line = lines.get(i, 0);
                angles[i] = line[1];
            }

            double angleVariance = calculateVariance(angles);

            if (angleVariance < 0.1) {
                return false;
            }

            Mat hierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            if (contours.size() < 50) return false;

            return true;

        } catch (Exception e) {
            System.err.println("Erro na validação da impressão digital: " + e.getMessage());
            return false;
        }
    }

    private double calculateVariance(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        double mean = sum / values.length;

        double squaredDifferenceSum = 0;
        for (double value : values) {
            squaredDifferenceSum += Math.pow(value - mean, 2);
        }

        return squaredDifferenceSum / values.length;
    }

    private String generateFingerprintHash(byte[] imageData) {
        try {
            Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_GRAYSCALE);

            Mat resizedImage = new Mat();
            Imgproc.resize(image, resizedImage, new Size(200, 200));

            Map<String, Object> features = extractSimpleFeatures(resizedImage);

            String featuresStr = features.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(featuresStr.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash da impressão digital", e);
        }
    }

    private Map<String, Object> extractSimpleFeatures(Mat image) {
        Map<String, Object> features = new HashMap<>();

        int regionsX = 5, regionsY = 5;
        int regionWidth = image.cols() / regionsX;
        int regionHeight = image.rows() / regionsY;

        for (int y = 0; y < regionsY; y++) {
            for (int x = 0; x < regionsX; x++) {
                Rect region = new Rect(
                        x * regionWidth,
                        y * regionHeight,
                        regionWidth,
                        regionHeight
                );

                Mat regionMat = new Mat(image, region);
                MatOfDouble mean = new MatOfDouble();
                MatOfDouble stdDev = new MatOfDouble();
                Core.meanStdDev(regionMat, mean, stdDev);

                String key = "region_" + x + "_" + y;
                double[] meanVal = mean.toArray();
                double[] stdDevVal = stdDev.toArray();

                features.put(key + "_mean", Math.round(meanVal[0] * 100) / 100.0);
                features.put(key + "_stddev", Math.round(stdDevVal[0] * 100) / 100.0);
            }
        }

        MatOfDouble globalMean = new MatOfDouble();
        MatOfDouble globalStdDev = new MatOfDouble();
        Core.meanStdDev(image, globalMean, globalStdDev);

        features.put("global_mean", Math.round(globalMean.toArray()[0] * 100) / 100.0);
        features.put("global_stddev", Math.round(globalStdDev.toArray()[0] * 100) / 100.0);

        return features;
    }
}