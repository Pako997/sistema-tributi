package it.pa.tributi.tributiservice.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Posizione.nuova")
class PosizioneTest {

    private static final BigDecimal IMPORTO = new BigDecimal("80.00");
    private static final LocalDate SCADENZA = LocalDate.of(2026, 12, 31);

    @Test
    @DisplayName("copia i dati ricevuti")
    void datiRicevuti() {
        Ente ente = ente();

        Posizione p = nuova(ente);

        assertThat(p.getIuv()).isEqualTo("17000000000001234");
        assertThat(p.getCodiceFiscale()).isEqualTo("RSSMRA80A01F839X");
        assertThat(p.getTipoTributo()).isEqualTo("MULTA");
        assertThat(p.getImporto()).isEqualByComparingTo("80.00");
        assertThat(p.getDataScadenza()).isEqualTo(SCADENZA);
        assertThat(p.getEnte()).isSameAs(ente);
    }

    @Test
    @DisplayName("nasce nello stato DA_PAGARE")
    void statoIniziale() {
        assertThat(nuova(ente()).getStato()).isEqualTo("DA_PAGARE");
    }

    @Test
    @DisplayName("l'importo originale coincide con l'importo (stesso oggetto)")
    void importoOriginale() {
        Posizione p = nuova(ente());

        assertThat(p.getImportoOriginale()).isSameAs(p.getImporto());
    }

    @Test
    @DisplayName("la data di emissione è oggi")
    void dataEmissioneOggi() {
        LocalDate prima = LocalDate.now();

        Posizione p = nuova(ente());

        assertThat(p.getDataEmissione()).isBetween(prima, LocalDate.now());
    }

    @Test
    @DisplayName("non è ancora pagata e non è ancora salvata: data di pagamento e id null")
    void nonPagataNonSalvata() {
        Posizione p = nuova(ente());

        assertThat(p.getDataPagamento()).isNull();
        assertThat(p.getId()).isNull();
    }

    @Test
    @DisplayName("ogni chiamata crea una posizione nuova")
    void istanzaNuova() {
        Ente ente = ente();

        assertThat(nuova(ente)).isNotSameAs(nuova(ente));
    }

    @Test
    @DisplayName("nessun controllo sui dati: valori null accettati, lo stato resta DA_PAGARE")
    void nessunControllo() {
        Posizione p = Posizione.nuova(null, null, null, null, null, null);

        assertThat(p.getIuv()).isNull();
        assertThat(p.getImporto()).isNull();
        assertThat(p.getImportoOriginale()).isNull();
        assertThat(p.getEnte()).isNull();
        assertThat(p.getStato()).isEqualTo("DA_PAGARE");
        assertThat(p.getDataEmissione()).isNotNull();
    }


    private static Posizione nuova(Ente ente) {
        return Posizione.nuova("17000000000001234", "RSSMRA80A01F839X", "MULTA", IMPORTO, SCADENZA, ente);
    }

    private static Ente ente() {
        Ente ente = new Ente();
        ente.setCodiceEnte("C_F839");
        return ente;
    }
}