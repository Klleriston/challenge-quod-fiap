package com.fiap.challengefiapquod.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometriaRequestDTO {
    private String imageUrl;
    private DispositivoInfo dispositivo;
    private Localizacao localizacao;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispositivoInfo {
        private String fabricante;
        private String modelo;
        private String sistemaOperacional;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Localizacao {
        private Double latitude;
        private Double longitude;
        private String ipOrigem;
    }
}
