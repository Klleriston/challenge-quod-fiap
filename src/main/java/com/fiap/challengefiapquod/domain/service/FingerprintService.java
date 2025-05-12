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

            if (image.width() < 200 || image.height() < 200 || image.width() > 5000 || image.height() > 5000) {
                return false;
            }

            Mat preprocessedImage = new Mat();

            Imgproc.equalizeHist(image, preprocessedImage);

            Imgproc.GaussianBlur(preprocessedImage, preprocessedImage, new Size(3, 3), 0);

            Mat edges = new Mat();
            Imgproc.Canny(preprocessedImage, edges, 50, 150);

            Mat lines = new Mat();
            Imgproc.HoughLinesP(edges, lines, 1, Math.PI/180, 50, 30, 10);

            if (lines.rows() < 10) {
                return false;
            }

            double[] lineAngles = extractLineOrientations(lines);

            double angleVariance = calculateAngleVariance(lineAngles);

            if (angleVariance < 0.1) {
                return false;
            }

            double linesDensity = calculateLinesDensity(image, lines);

            return linesDensity > 0.1 && linesDensity < 0.7;

        } catch (Exception e) {
            System.err.println("Erro na validação da impressão digital: " + e.getMessage());
            return false;
        }
    }

    private double[] extractLineOrientations(Mat lines) {
        double[] angles = new double[lines.rows()];

        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);

            double dx = line[2] - line[0];
            double dy = line[3] - line[1];

            double angle = Math.atan2(dy, dx);

            angles[i] = Math.abs(angle);
        }

        return angles;
    }

    private double calculateAngleVariance(double[] angles) {
        if (angles.length == 0) return 0;
        double sum = 0;
        for (double angle : angles) {
            sum += angle;
        }
        double mean = sum / angles.length;

        double squaredDifferenceSum = 0;
        for (double angle : angles) {
            squaredDifferenceSum += Math.pow(angle - mean, 2);
        }

        return squaredDifferenceSum / angles.length;
    }

    private double calculateLinesDensity(Mat image, Mat lines) {
        Mat linesMask = Mat.zeros(image.size(), CvType.CV_8UC1);

        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);
            Imgproc.line(linesMask,
                    new Point(line[0], line[1]),
                    new Point(line[2], line[3]),
                    new Scalar(255),
                    2
            );
        }

        int linePixels = Core.countNonZero(linesMask);
        int totalPixels = image.rows() * image.cols();

        return (double) linePixels / totalPixels;
    }

}