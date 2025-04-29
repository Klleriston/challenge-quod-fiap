package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.domain.model.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Service
public class ImageAnalysisService {

    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private CascadeClassifier profileFaceCascade;
    private FaceLivenessDetector livenessDetector;
    private final ExifAnalysisService exifAnalysisService = new ExifAnalysisService();

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

        livenessDetector = new FaceLivenessDetector(faceCascade, eyesCascade);
    }

    public CascadeClassifier getFaceCascade() {
        return faceCascade;
    }

    public CascadeClassifier getEyesCascade() {
        return eyesCascade;
    }

    public CascadeClassifier getProfileFaceCascade() {
        return profileFaceCascade;
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
    }

    public Image analyzeImage(MultipartFile file, String userId) throws IOException {
        Image image = new Image();
        image.setUserId(userId);
        image.setFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setData(file.getBytes());

        boolean isSelfie = detectSelfie(file.getBytes());
        boolean isVerified = verifyImage(file.getBytes());

        image.setSelfie(isSelfie);
        image.setVerified(isVerified);

        if (isSelfie && isVerified) {
            image.setVerificationStatus("APPROVED");
        } else if (!isSelfie) {
            image.setVerificationStatus("REJECTED_NOT_SELFIE");
        } else {
            image.setVerificationStatus("REJECTED_POSSIBLE_FRAUD");
        }

        return image;
    }

    private boolean detectSelfie(byte[] imageData) {
        try {
            Mat image = bytesToMat(imageData);

            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayImage, grayImage);

            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(grayImage, faces, 1.1, 3, 0, new Size(30, 30), new Size());

            if (faces.empty() || faces.toArray().length == 0) {
                return false;
            }

            Rect[] facesArray = faces.toArray();

            if (facesArray.length > 1) {
                Rect largestFace = getLargestFace(facesArray);

                double totalFaceArea = 0;
                for (Rect face : facesArray) {
                    totalFaceArea += face.area();
                }

                if (largestFace.area() / totalFaceArea < 0.7) {
                    return false;
                }
            }

            Rect mainFace = facesArray.length == 1 ? facesArray[0] : getLargestFace(facesArray);

            double faceArea = mainFace.area();
            double imageArea = image.rows() * image.cols();
            double faceRatio = faceArea / imageArea;

            if (faceRatio < 0.10) {
                return false;
            }

            Point faceCenter = new Point(
                    mainFace.x + mainFace.width / 2.0,
                    mainFace.y + mainFace.height / 2.0
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

            if (relativeDistance > 0.3) {
                return false;
            }

            Mat faceROI = grayImage.submat(mainFace);
            MatOfRect eyes = new MatOfRect();
            eyesCascade.detectMultiScale(faceROI, eyes);

            if (eyes.empty() || eyes.toArray().length < 2) {
                return false;
            }

            boolean isLive = livenessDetector.detectLiveness(image);

            return isLive;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Rect getLargestFace(Rect[] faces) {
        Rect largest = faces[0];
        for (Rect face : faces) {
            if (face.area() > largest.area()) {
                largest = face;
            }
        }
        return largest;
    }

    private boolean verifyImage(byte[] imageData) {
        try {
            if (!isValidImageFormat(imageData)) {
                return false;
            }

            Mat image = bytesToMat(imageData);

            if (image.width() < 200 || image.height() < 200) {
                return false;
            }

            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            Mat laplacian = new Mat();
            Imgproc.Laplacian(grayImage, laplacian, 3);

            MatOfDouble mean = new MatOfDouble();
            MatOfDouble stddev = new MatOfDouble();
            Core.meanStdDev(laplacian, mean, stddev);
            double variance = Math.pow(stddev.get(0, 0)[0], 2);

            if (variance < 100) {
                return false;
            }

            if (isJpegFormat(imageData)) {
                boolean recentPhoto = exifAnalysisService.verifyExifMetadata(imageData, 24 * 60 * 60 * 1000);

                boolean validCamera = exifAnalysisService.checkCameraSource(imageData);

                if (!recentPhoto && !validCamera) {
                    return false;
                }
            }

            Mat hist = new Mat();
            Mat hsv = new Mat();
            Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

            Imgproc.calcHist(
                    Arrays.asList(hsv),
                    new MatOfInt(0),
                    new Mat(),
                    hist,
                    new MatOfInt(30),
                    new MatOfFloat(0, 180)
            );

            Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);

            float[] histData = new float[(int)(hist.total() * hist.channels())];
            hist.get(0, 0, histData);

            int peaks = 0;
            for (int i = 0; i < histData.length; i++) {
                if (histData[i] > 0.2) {
                    peaks++;
                }
            }

            if (peaks > 15) {
                return false;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isJpegFormat(byte[] imageData) {
        if (imageData.length < 2) {
            return false;
        }

        return imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8;
    }

    private Mat bytesToMat(byte[] imageData) {
        MatOfByte mob = new MatOfByte(imageData);
        return Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
    }

    private boolean isValidImageFormat(byte[] imageData) {
        List<byte[]> jpegSignatures = Arrays.asList(
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0},
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1}
        );

        byte[] pngSignature = new byte[]{(byte) 0x89, 'P', 'N', 'G'};

        if (imageData.length < 4) {
            return false;
        }

        byte[] header = Arrays.copyOfRange(imageData, 0, 4);

        for (byte[] signature : jpegSignatures) {
            if (Arrays.equals(Arrays.copyOfRange(header, 0, signature.length), signature)) {
                return true;
            }
        }

        if (Arrays.equals(header, pngSignature)) {
            return true;
        }

        return false;
    }
}