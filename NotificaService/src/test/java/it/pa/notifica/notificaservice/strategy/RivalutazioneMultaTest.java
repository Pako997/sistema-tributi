package it.pa.notifica.notificaservice.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import it.pa.notifica.notificaservice.config.RivalutazioneProperties;
import it.pa.notifica.notificaservice.entity.Posizione;

@DisplayName("RivalutazioneMulta")
class RivalutazioneMultaTest {

    private RivalutazioneProperties props;
    private RivalutazioneMulta strategia;

    @BeforeEach
    void crea() {
        props = new RivalutazioneProperties();
        props.setMultaGiorniTermine(60);
        props.setMultaGiorniSemestre(180);
        props.setMultaMaggiorazioneSemestrale(new BigDecimal("0.10"));
        props.setMultaTettoMaggiorazione(new BigDecimal("0.60"));
        strategia = new RivalutazioneMulta(props);
    }

    @Test
    @DisplayName("tipo tributo: MULTA")
    void tipoTributo() {
        assertThat(strategia.tipoTributo()).isEqualTo("MULTA");
    }


    @Nested
    @DisplayName("Entro il termine")
    class EntroTermine {

        @ParameterizedTest(name = "{0} giorni: importo originale")
        @CsvSource({"-10", "0", "1", "59", "60"})
        void importoOriginale(long giorni) {
            assertThat(strategia.calcolaImporto(multa("100.00"), giorni)).isEqualTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("restituisce lo stesso oggetto importoOriginale, senza arrotondarlo")
        void stessoOggetto() {
            Posizione p = multa("100");

            BigDecimal risultato = strategia.calcolaImporto(p, 30);

            assertThat(risultato).isSameAs(p.getImportoOriginale());
            assertThat(risultato.scale()).isZero();
        }

        @Test
        @DisplayName("importo originale null: restituisce null")
        void originaleNull() {
            Posizione p = multa("100.00");
            p.setImportoOriginale(null);

            assertThat(strategia.calcolaImporto(p, 30)).isNull();
        }
    }


    @Nested
    @DisplayName("Oltre il termine")
    class OltreTermine {

        @ParameterizedTest(name = "{0} giorni su 100.00 -> {1}")
        @CsvSource({
                "61,   200.00",
                "239,  200.00",
                "240,  220.00",
                "420,  240.00",
                "1140, 320.00",
                "1320, 320.00",
                "9999, 320.00"
        })
        void importoRivalutato(long giorni, String atteso) {
            assertThat(strategia.calcolaImporto(multa("100.00"), giorni)).isEqualTo(new BigDecimal(atteso));
        }

        @Test
        @DisplayName("si basa sull'importo originale, non su quello attuale")
        void baseImportoOriginale() {
            Posizione p = multa("100.00");
            p.setImporto(new BigDecimal("999.00"));

            assertThat(strategia.calcolaImporto(p, 240)).isEqualTo(new BigDecimal("220.00"));
        }

        @Test
        @DisplayName("risultato arrotondato a due decimali HALF_UP")
        void arrotondamento() {
            assertThat(strategia.calcolaImporto(multa("33.33"), 240)).isEqualTo(new BigDecimal("73.33"));
        }

        @Test
        @DisplayName("importo originale null: NullPointerException")
        void originaleNull() {
            Posizione p = multa("100.00");
            p.setImportoOriginale(null);

            assertThatThrownBy(() -> strategia.calcolaImporto(p, 100))
                    .isInstanceOf(NullPointerException.class);
        }
    }


    @Nested
    @DisplayName("Parametri di configurazione")
    class Parametri {

        @Test
        @DisplayName("termine diverso: il raddoppio scatta dal giorno dopo il nuovo termine")
        void termineConfigurabile() {
            props.setMultaGiorniTermine(30);

            assertThat(strategia.calcolaImporto(multa("100.00"), 30)).isEqualTo(new BigDecimal("100.00"));
            assertThat(strategia.calcolaImporto(multa("100.00"), 31)).isEqualTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("maggiorazione e tetto diversi vengono applicati")
        void maggiorazioneETetto() {
            props.setMultaMaggiorazioneSemestrale(new BigDecimal("0.20"));
            props.setMultaTettoMaggiorazione(new BigDecimal("0.30"));

            assertThat(strategia.calcolaImporto(multa("100.00"), 240)).isEqualTo(new BigDecimal("240.00"));
            assertThat(strategia.calcolaImporto(multa("100.00"), 420)).isEqualTo(new BigDecimal("260.00"));
        }

        @Test
        @DisplayName("i parametri vengono letti a ogni calcolo, non alla costruzione")
        void letturaAOgniCalcolo() {
            assertThat(strategia.calcolaImporto(multa("100.00"), 61)).isEqualTo(new BigDecimal("200.00"));

            props.setMultaGiorniTermine(90);

            assertThat(strategia.calcolaImporto(multa("100.00"), 61)).isEqualTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("durata del semestre 0: ArithmeticException oltre il termine")
        void semestreZero() {
            props.setMultaGiorniSemestre(0);

            Posizione p = multa("100.00");

            assertThatThrownBy(() -> strategia.calcolaImporto(p, 61))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    private static Posizione multa(String importoOriginale) {
        Posizione p = new Posizione();
        p.setIuv("IUV-MULTA");
        p.setTipoTributo("MULTA");
        p.setImporto(new BigDecimal(importoOriginale));
        p.setImportoOriginale(new BigDecimal(importoOriginale));
        return p;
    }
}