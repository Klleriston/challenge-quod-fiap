package com.fiap.challengefiapquod.presentation.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotificacaoException extends RuntimeException {

    private final HttpStatus status;

    public NotificacaoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static NotificacaoException falhaAoNotificar() {
        return new NotificacaoException("Falha ao enviar notificação de fraude", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static NotificacaoException dadosInvalidos() {
        return new NotificacaoException("Dados inválidos para notificação", HttpStatus.BAD_REQUEST);
    }
}