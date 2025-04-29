package com.fiap.challengefiapquod.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalysisResultDTO {
    private String analysisId;
    private String imageDescription;
    private boolean containsFaces;
    private int faceCount;
    private boolean isFraud;
    private String fraudReason;
    private String imageType;
    private int imageWidth;
    private int imageHeight;
    private List<FaceDescriptionDTO> faces;
}