package com.fiap.challengefiapquod.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintResponseDTO {
    private String message;
    private boolean isFraud;
}