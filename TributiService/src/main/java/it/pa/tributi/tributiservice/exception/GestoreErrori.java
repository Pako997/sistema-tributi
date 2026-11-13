package it.pa.tributi.tributiservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GestoreErrori {

    @ExceptionHandler(TributiException.class)
    public ResponseEntity<Map<String, String>> gestisci(TributiException e) {

        HttpStatus stato = switch (e.getErrore()) {
            case AUTENTICAZIONE_MANCANTE -> HttpStatus.UNAUTHORIZED;
            case UTENTE_NON_AUTORIZZATO, FORBIDDEN -> HttpStatus.FORBIDDEN;
            case ENTE_NON_TROVATO, IUV_NON_TROVATO -> HttpStatus.NOT_FOUND;
            case PARAMETRI_MANCANTI, IMPORTO_NULLO, DATA_SFORMATA,
                 DATA_NON_PARSABILE, SCADENZA_NON_VALIDA -> HttpStatus.BAD_REQUEST;
            case PAGAMENTO_NON_RIUSCITO -> HttpStatus.UNPROCESSABLE_ENTITY;
            case ERRORE_INTERNO -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return rispondi(stato, e.getErrore());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> gestisciImprevisto(Exception e) {
        return rispondi(HttpStatus.INTERNAL_SERVER_ERROR, ErroreTributi.ERRORE_INTERNO);
    }

    private ResponseEntity<Map<String, String>> rispondi(HttpStatus stato, ErroreTributi errore) {
        return ResponseEntity.status(stato)
                .body(Map.of("codice", errore.getCodice(),
                        "messaggio", errore.getMessaggio()));
    }
}
