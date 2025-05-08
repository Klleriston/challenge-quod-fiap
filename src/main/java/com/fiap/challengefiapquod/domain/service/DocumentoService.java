package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.application.dto.DocumentRequestDTO;
import com.fiap.challengefiapquod.application.dto.DocumentResponseDTO;
import com.fiap.challengefiapquod.domain.model.Document;
import com.fiap.challengefiapquod.domain.model.NotificacaoFraude;
import com.fiap.challengefiapquod.domain.repository.DocumentRepository;
import com.fiap.challengefiapquod.domain.repository.NotificationFraudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentRepository documentRepository;
    private final ValidadorCpfService validadorCpfService;
    private final ValidadorRgService validadorRgService;
    private final NotificationService notificationService;
    private final NotificationFraudRepository notificationFraudRepository;

    private static final String QUOD_API_URL = "https://api.quod.com.br/api/notificacoes/fraude";

    public DocumentResponseDTO validarDocumento(DocumentRequestDTO request) {
        String cpf = request.getCpf();
        String rg = request.getRg();

        boolean cpfValido = validadorCpfService.validarCpf(cpf);
        boolean rgValido = validadorRgService.validarRg(rg);

        if (cpfValido && rgValido) {
            Document document = Document.criarDocumentoValido(cpf, rg);
            documentRepository.save(document);

            notificationService.registrarProcessamentoSucesso(
                    "documento",
                    "CPF: " + cpf.substring(0, 3) + ".***.***-**"
            );

            return DocumentResponseDTO.builder()
                    .status("SUCESSO")
                    .mensagem("Documento validado com sucesso")
                    .build();
        } else {
            Document document = Document.criarDocumentoInvalido(cpf, rg);
            documentRepository.save(document);

            String tipoFraude = determinarTipoFraude(cpfValido, rgValido);

            Map<String, Object> metadados = new HashMap<>();
            metadados.put("cpfMascarado", cpf.substring(0, 3) + ".***.***-**");
            metadados.put("rgMascarado", "**.***.***-*");

            String transactionId = notificationService.notificarFraude("documento", tipoFraude, metadados);

            NotificacaoFraude notification = notificationFraudRepository.findByTransacaoId(transactionId);

            String jsonNotification = convertNotificacaoToJson(notification);

            return DocumentResponseDTO.builder()
                    .status("FRAUDE")
                    .mensagem("Documento inválido detectado: " + tipoFraude + ". " +
                            "Uma notificação foi enviada para " + QUOD_API_URL + " com os dados: " + jsonNotification)
                    .build();
        }
    }

    private String determinarTipoFraude(boolean cpfValido, boolean rgValido) {
        if (!cpfValido && !rgValido) {
            return "documentos_invalidos";
        } else if (!cpfValido) {
            return "cpf_invalido";
        } else {
            return "rg_invalido";
        }
    }

    private String convertNotificacaoToJson(NotificacaoFraude notification) {
        if (notification == null) {
            return "{}";
        }

        String dataCaptura = notification.getDataCaptura().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return "{\n" +
                "  \"transacaoId\": \"" + notification.getTransacaoId() + "\",\n" +
                "  \"tipoBiometria\": \"" + notification.getTipoBiometria() + "\",\n" +
                "  \"tipoFraude\": \"" + notification.getTipoFraude() + "\",\n" +
                "  \"dataCaptura\": \"" + dataCaptura + "\",\n" +
                "  \"dispositivo\": {\n" +
                "    \"fabricante\": \"" + notification.getDispositivo().getFabricante() + "\",\n" +
                "    \"modelo\": \"" + notification.getDispositivo().getModelo() + "\",\n" +
                "    \"sistemaOperacional\": \"" + notification.getDispositivo().getSistemaOperacional() + "\"\n" +
                "  },\n" +
                "  \"canalNotificacao\": [\"" + String.join("\", \"", notification.getCanalNotificacao()) + "\"],\n" +
                "  \"notificadoPor\": \"" + notification.getNotificadoPor() + "\",\n" +
                "  \"metadados\": " + convertMapToJson(notification.getMetadados()) + "\n" +
                "}";
    }

    private String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;

            sb.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("\"").append(value).append("\"");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}