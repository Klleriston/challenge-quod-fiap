package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.application.dto.BiometriaRequestDTO;
import com.fiap.challengefiapquod.application.dto.BiometriaResponseDTO;

public interface BiometriaService {
    BiometriaResponseDTO validarBiometria(BiometriaRequestDTO request, String userId);
}