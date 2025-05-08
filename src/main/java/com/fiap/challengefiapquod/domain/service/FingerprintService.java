package com.fiap.challengefiapquod.domain.service;

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

}