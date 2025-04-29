package com.fiap.challengefiapquod.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceDescriptionDTO {
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean hasEyes;
    private boolean isFrontal;
}