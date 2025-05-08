package com.fiap.challengefiapquod.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalysisResultDTO {
    private String analysisId;
    private String description;
    private boolean containsFaces;
    private int faceCount;
    private List<FaceDescriptionDTO> faces;
    private String imageType;
    private int imageWidth;
    private int imageHeight;
    private boolean fraud = false;
    private String fraudReason;
}