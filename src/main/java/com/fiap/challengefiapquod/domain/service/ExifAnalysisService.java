package com.fiap.challengefiapquod.domain.service;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;

import java.util.Date;

public class ExifAnalysisService {

    public boolean verifyExifMetadata(byte[] imageData, long maxAgeInMillis) {
        try {
            ImageMetadata metadata = Imaging.getMetadata(imageData);

            if (metadata == null || !(metadata instanceof JpegImageMetadata)) {
                return false;
            }

            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

            TiffField dateTimeField = jpegMetadata.findEXIFValueWithExactMatch(
                    ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);

            if (dateTimeField == null) {
                return false;
            }

            String dateTimeStr = dateTimeField.getStringValue();
            Date photoDate = parseExifDate(dateTimeStr);

            if (photoDate == null) {
                return false;
            }

            long currentTime = System.currentTimeMillis();
            long photoTime = photoDate.getTime();

            return (currentTime - photoTime) <= maxAgeInMillis;

        } catch (Exception e) {
            return false;
        }
    }

    private Date parseExifDate(String exifDate) {
        try {
            String normalizedDate = exifDate.replace(":", "/");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return sdf.parse(normalizedDate);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean checkCameraSource(byte[] imageData) {
        try {
            ImageMetadata metadata = Imaging.getMetadata(imageData);

            if (metadata == null || !(metadata instanceof JpegImageMetadata)) {
                return false;
            }

            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            TiffImageMetadata exifMetadata = jpegMetadata.getExif();

            if (exifMetadata == null) {
                return false;
            }

            TiffField makeField = jpegMetadata.findEXIFValueWithExactMatch(
                    TiffTagConstants.TIFF_TAG_MAKE);
            TiffField modelField = jpegMetadata.findEXIFValueWithExactMatch(
                    TiffTagConstants.TIFF_TAG_MODEL);

            return makeField != null && modelField != null;

        } catch (Exception e) {
            return false;
        }
    }
}