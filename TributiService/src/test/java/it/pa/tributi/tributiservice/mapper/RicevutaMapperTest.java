package it.pa.tributi.tributiservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.pa.tributi.tributiservice.dto.RicevutaDTO;
import it.pa.tributi.tributiservice.entity.Ente;
import it.pa.tributi.tributiservice.entity.Posizione;

@DisplayName("RicevutaMapper")
class RicevutaMapperTest {

    private static final String TRX = "TRX-001";
    private static final BigDecimal VERSATO = new BigDecimal("130.00");
    private static final LocalDate OGGI = LocalDate.of(2026, 2, 20);

    private final RicevutaMapper mapper = new RicevutaMapper();

    @Test
    @DisplayName("copia i dati della posizione, dell'ente e del pagamento")
    void tuttiICampi() {
        RicevutaDTO r = mapper.toRicevuta(posizione(), TRX, VERSATO, OGGI);

        assertThat(r.getCodiceFiscale()).isEqualTo("RSSMRA80A01F839X");
        assertThat(r.getTipoTributo()).isEqualTo("TARI");
        assertThat(r.getIuv()).isEqualTo("IUV-1");
        assertThat(r.getIdTransazione()).isEqualTo(TRX);
        assertThat(r.getImporto()).isEqualByComparingTo("130.00");
        assertThat(r.getCodiceEnte()).isEqualTo("C_F839");
        assertThat(r.getEnteEmittente()).isEqualTo("Comune di Napoli");
        assertThat(r.getContattoEnte()).isEqualTo("tributi@comune.napoli.it");
        assertThat(r.getDataPagamento()).isEqualTo(OGGI);
    }

    @Test
    @DisplayName("l'importo è quello versato, non quello della posizione")
    void importoVersato() {
        Posizione p = posizione();
        p.setImporto(new BigDecimal("999.00"));

        RicevutaDTO r = mapper.toRicevuta(p, TRX, VERSATO, OGGI);

        assertThat(r.getImporto()).isSameAs(VERSATO);
    }

    @Test
    @DisplayName("la data è quella passata, non la data di pagamento della posizione")
    void dataPassata() {
        Posizione p = posizione();
        p.setDataPagamento(LocalDate.of(2020, 1, 1));

        RicevutaDTO r = mapper.toRicevuta(p, TRX, VERSATO, OGGI);

        assertThat(r.getDataPagamento()).isEqualTo(OGGI);
    }

    @Test
    @DisplayName("id transazione, importo e data null: copiati come null, nessun controllo")
    void valoriNull() {
        RicevutaDTO r = mapper.toRicevuta(posizione(), null, null, null);

        assertThat(r.getIdTransazione()).isNull();
        assertThat(r.getImporto()).isNull();
        assertThat(r.getDataPagamento()).isNull();
    }

    @Test
    @DisplayName("contatto ente null: resta null")
    void contattoNull() {
        Posizione p = posizione();
        p.getEnte().setContatto(null);

        assertThat(mapper.toRicevuta(p, TRX, VERSATO, OGGI).getContattoEnte()).isNull();
    }

    @Test
    @DisplayName("ogni chiamata crea una ricevuta nuova")
    void ricevutaNuova() {
        Posizione p = posizione();

        assertThat(mapper.toRicevuta(p, TRX, VERSATO, OGGI))
                .isNotSameAs(mapper.toRicevuta(p, TRX, VERSATO, OGGI));
    }

    @Test
    @DisplayName("posizione senza ente: NullPointerException")
    void senzaEnte() {
        Posizione p = posizione();
        p.setEnte(null);

        assertThatThrownBy(() -> mapper.toRicevuta(p, TRX, VERSATO, OGGI))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("posizione null: NullPointerException")
    void posizioneNull() {
        assertThatThrownBy(() -> mapper.toRicevuta(null, TRX, VERSATO, OGGI))
                .isInstanceOf(NullPointerException.class);
    }


    private static Posizione posizione() {
        Ente ente = new Ente();
        ente.setCodiceEnte("C_F839");
        ente.setDenominazione("Comune di Napoli");
        ente.setContatto("tributi@comune.napoli.it");

        Posizione p = new Posizione();
        p.setIuv("IUV-1");
        p.setCodiceFiscale("RSSMRA80A01F839X");
        p.setTipoTributo("TARI");
        p.setImporto(new BigDecimal("100.00"));
        p.setEnte(ente);
        return p;
    }
}