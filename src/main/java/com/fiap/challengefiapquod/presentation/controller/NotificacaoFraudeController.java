package com.fiap.challengefiapquod.presentation.controller;

import com.fiap.challengefiapquod.application.dto.NotificationFraudRequestDTO;
import com.fiap.challengefiapquod.application.dto.NotificationFraudResponseDTO;
import com.fiap.challengefiapquod.domain.model.NotificacaoFraude;
import com.fiap.challengefiapquod.domain.repository.NotificationFraudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoFraudeController {

    private final NotificationFraudRepository notificacaoFraudeRepository;

    @PostMapping("/fraude")
    public ResponseEntity<NotificationFraudResponseDTO> notificarFraude(
            @RequestBody NotificationFraudRequestDTO request) {

        try {
            if (request.getTransacaoId() == null || request.getTransacaoId().isEmpty()) {
                request.setTransacaoId(UUID.randomUUID().toString());
            }

            NotificacaoFraude notificacao = convertToEntity(request);
            notificacaoFraudeRepository.save(notificacao);

            NotificationFraudResponseDTO response = NotificationFraudResponseDTO.builder()
                    .transacaoId(notificacao.getTransacaoId())
                    .sucesso(true)
                    .mensagem("Notificação de fraude registrada com sucesso")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            NotificationFraudResponseDTO response = NotificationFraudResponseDTO.builder()
                    .transacaoId(request.getTransacaoId())
                    .sucesso(false)
                    .mensagem("Erro ao registrar notificação: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoFraude>> listarNotificacoes() {
        return ResponseEntity.ok(notificacaoFraudeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificacaoFraude> buscarNotificacao(@PathVariable String id) {
        return notificacaoFraudeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private NotificacaoFraude convertToEntity(NotificationFraudRequestDTO request) {
        LocalDateTime dataCaptura;
        if (request.getDataCaptura() != null && !request.getDataCaptura().isEmpty()) {
            dataCaptura = ZonedDateTime.parse(
                    request.getDataCaptura(),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
            ).toLocalDateTime();
        } else {
            dataCaptura = LocalDateTime.now();
        }

        NotificacaoFraude.DispositivoInfo dispositivo = getDispositivoInfo(request);

        Map<String, Object> metadados = new HashMap<>();
        if (request.getMetadados() != null) {
            metadados.put("latitude", request.getMetadados().getLatitude());
            metadados.put("longitude", request.getMetadados().getLongitude());
            metadados.put("ipOrigem", request.getMetadados().getIpOrigem());
        }

        return NotificacaoFraude.builder()
                .transacaoId(request.getTransacaoId())
                .tipoBiometria(request.getTipoBiometria())
                .tipoFraude(request.getTipoFraude())
                .dataCaptura(dataCaptura)
                .dispositivo(dispositivo)
                .canalNotificacao(request.getCanalNotificacao())
                .notificadoPor(request.getNotificadoPor())
                .metadados(metadados)
                .processada(false)
                .dataCriacao(LocalDateTime.now())
                .build();
    }

    private static NotificacaoFraude.DispositivoInfo getDispositivoInfo(NotificationFraudRequestDTO request) {
        NotificacaoFraude.DispositivoInfo dispositivo = new NotificacaoFraude.DispositivoInfo();
        if (request.getDispositivo() != null) {
            dispositivo.setFabricante(request.getDispositivo().getFabricante());
            dispositivo.setModelo(request.getDispositivo().getModelo());
            dispositivo.setSistemaOperacional(request.getDispositivo().getSistemaOperacional());
        } else {
            dispositivo.setFabricante("Desconhecido");
            dispositivo.setModelo("Desconhecido");
            dispositivo.setSistemaOperacional("Desconhecido");
        }
        return dispositivo;
    }
}