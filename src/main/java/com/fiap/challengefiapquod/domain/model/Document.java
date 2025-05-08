package com.fiap.challengefiapquod.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "documentos")
public class Document {
    @Id
    private String id;
    private String cpf;
    private String rg;
    private boolean valido;
    private LocalDateTime dataVerificacao;

    public static Document criarDocumentoValido(String cpf, String rg) {
        return Document.builder()
                .cpf(cpf)
                .rg(rg)
                .valido(true)
                .dataVerificacao(LocalDateTime.now())
                .build();
    }

    public static Document criarDocumentoInvalido(String cpf, String rg) {
        return Document.builder()
                .cpf(cpf)
                .rg(rg)
                .valido(false)
                .dataVerificacao(LocalDateTime.now())
                .build();
    }
}