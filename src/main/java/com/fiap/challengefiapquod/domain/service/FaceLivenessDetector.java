package com.fiap.challengefiapquod.domain.service;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.List;

public class FaceLivenessDetector {

    private final CascadeClassifier faceCascade;
    private final CascadeClassifier eyesCascade;

    public FaceLivenessDetector(CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {
        this.faceCascade = faceCascade;
        this.eyesCascade = eyesCascade;
    }

    public boolean detectLiveness(Mat image) {
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, gray);

        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(30, 30), new Size());

        if (faces.empty()) {
            return false;
        }

        Rect[] facesArray = faces.toArray();
        if (facesArray.length == 0) {
            return false;
        }

        Rect faceRect = getLargestFace(facesArray);
        Mat faceROI = gray.submat(faceRect);

        boolean textureCheck = checkSkinTexture(faceROI);

        boolean brightnessCheck = checkBrightnessPattern(faceROI);

        boolean eyesCheck = checkEyesDetection(faceROI);

        boolean proportionsCheck = checkFacialProportions(faceRect, image.size());

        int passedChecks = (textureCheck ? 1 : 0) +
                (brightnessCheck ? 1 : 0) +
                (eyesCheck ? 1 : 0) +
                (proportionsCheck ? 1 : 0);

        return passedChecks >= 3;
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

    private boolean checkSkinTexture(Mat faceROI) {
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Imgproc.Sobel(faceROI, gradX, CvType.CV_32F, 1, 0);
        Imgproc.Sobel(faceROI, gradY, CvType.CV_32F, 0, 1);

        Mat magnitude = new Mat();
        Core.cartToPolar(gradX, gradY, magnitude, new Mat());
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(magnitude, mean, stddev);

        double meanVal = mean.get(0, 0)[0];
        double stdVal = stddev.get(0, 0)[0];
        return meanVal > 10 && meanVal < 40 && stdVal > 15 && stdVal < 45;
    }

    private boolean checkBrightnessPattern(Mat faceROI) {
        Mat hist = new Mat();
        Imgproc.calcHist(
                List.of(faceROI),
                new MatOfInt(0),
                new Mat(),
                hist,
                new MatOfInt(256),
                new MatOfFloat(0, 256)
        );

        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);

        float[] histData = new float[(int)(hist.total() * hist.channels())];
        hist.get(0, 0, histData);

        int peaks = 0;
        for (int i = 1; i < histData.length - 1; i++) {
            if (histData[i] > histData[i-1] && histData[i] > histData[i+1] && histData[i] > 0.05) {
                peaks++;
            }
        }

        return peaks < 10;
    }

    private boolean checkEyesDetection(Mat faceROI) {
        MatOfRect eyes = new MatOfRect();
        eyesCascade.detectMultiScale(faceROI, eyes);

        Rect[] eyesArray = eyes.toArray();
        if (eyesArray.length < 2) {
            return false;
        }

        List<Rect> validEyes = new ArrayList<>();
        for (Rect eye : eyesArray) {
            double relativeSize = (double) eye.area() / faceROI.size().area();
            if (relativeSize > 0.01 && relativeSize < 0.1) {
                validEyes.add(eye);
            }
        }

        return validEyes.size() >= 2;
    }

    private boolean checkFacialProportions(Rect faceRect, Size imageSize) {
        double aspectRatio = (double) faceRect.width / faceRect.height;

        if (aspectRatio < 0.6 || aspectRatio > 0.95) {
            return false;
        }

        Point faceCenter = new Point(
                faceRect.x + faceRect.width / 2.0,
                faceRect.y + faceRect.height / 2.0
        );

        Point imageCenter = new Point(
                imageSize.width / 2.0,
                imageSize.height / 2.0
        );

        double distanceToCenter = Math.sqrt(
                Math.pow(faceCenter.x - imageCenter.x, 2) +
                        Math.pow(faceCenter.y - imageCenter.y, 2)
        );

        double imageDiagonal = Math.sqrt(
                Math.pow(imageSize.width, 2) +
                        Math.pow(imageSize.height, 2)
        );

        double relativeDistance = distanceToCenter / imageDiagonal;

        return relativeDistance < 0.25;
    }
}