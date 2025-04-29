package com.fiap.challengefiapquod.application.dto;

import lombok.Data;

@Data
public class ImageDTO {
    private String id;
    private String userId;
    private String fileName;
    private boolean isSelfie;
    private boolean isVerified;
    private String verificationStatus;
}