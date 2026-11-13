package it.pa.tributi.tributiservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("ErroreTributi")
class ErroreTributiTest {

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource(delimiter = '|', quoteCharacter = '"', value = {
            "AUTENTICAZIONE_MANCANTE | AUTH_000  | Informazioni di autenticazione mancanti",
            "UTENTE_NON_AUTORIZZATO  | AUTH_001  | Utente non autorizzato",
            "ERRORE_INTERNO          | INT_001   | Errore interno",
            "FORBIDDEN               | FORB_001  | L'operazione richiesta non e' consentita",
            "ENTE_NON_TROVATO        | NFOU_001  | Ente non trovato",
            "PARAMETRI_MANCANTI      | PMISS_001 | Uno o piu parametri forniti non sono stati valorizzati",
            "IMPORTO_NULLO           | IMPO_001  | Importo nullo, impossibile procedere",
            "DATA_SFORMATA           | DATA_001  | La data fornita in input non segue il formato yyyy-MM-dd",
            "DATA_NON_PARSABILE      | DATA_002  | La data fornita in input non e' valida",
            "SCADENZA_NON_VALIDA     | DATA_003  | La data scadenza deve essere almeno a 30 giorni dalla data emissione",
            "IUV_NON_TROVATO         | NIUV_001  | Identificativo versamento non trovato",
            "PAGAMENTO_NON_RIUSCITO  | PERR_001  | Pagamento non riuscito. Verificare il metodo"
    })
    void codiceEMessaggio(ErroreTributi errore, String codice, String messaggio) {
        assertThat(errore.getCodice()).isEqualTo(codice);
        assertThat(errore.getMessaggio()).isEqualTo(messaggio);
    }

    @Test
    @DisplayName("ci sono 12 errori")
    void conteggio() {
        assertThat(ErroreTributi.values()).hasSize(12);
    }

    @Test
    @DisplayName("i codici sono tutti diversi tra loro")
    void codiciUnici() {
        assertThat(Arrays.stream(ErroreTributi.values()).map(ErroreTributi::getCodice))
                .doesNotHaveDuplicates();
    }

    @ParameterizedTest(name = "{0}: codice nel formato PREFISSO_NNN")
    @EnumSource(ErroreTributi.class)
    void formatoCodice(ErroreTributi errore) {
        assertThat(errore.getCodice()).matches("[A-Z]+_\\d{3}");
    }

    @ParameterizedTest(name = "{0}: messaggio non vuoto")
    @EnumSource(ErroreTributi.class)
    void messaggioNonVuoto(ErroreTributi errore) {
        assertThat(errore.getMessaggio()).isNotBlank();
    }
}