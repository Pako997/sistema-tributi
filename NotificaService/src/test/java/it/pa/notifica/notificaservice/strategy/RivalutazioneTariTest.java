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

@DisplayName("RivalutazioneTari")
class RivalutazioneTariTest {

    private RivalutazioneProperties props;
    private RivalutazioneTari strategia;

    @BeforeEach
    void crea() {
        props = new RivalutazioneProperties();
        props.setTassoLegale(new BigDecimal("1.60"));
        strategia = new RivalutazioneTari(props);
    }

    @Test
    @DisplayName("tipo tributo: TARI")
    void tipoTributo() {
        assertThat(strategia.tipoTributo()).isEqualTo("TARI");
    }


    @Nested
    @DisplayName("Importi per fascia di ritardo")
    class Fasce {

        @ParameterizedTest(name = "{0} giorni su 100.00 -> {1}")
        @CsvSource({
                "0,    100.00",
                "1,    100.08",
                "6,    100.53",
                "10,   100.87",
                "14,   101.23",
                "15,   101.32",
                "30,   101.38",
                "31,   101.53",
                "90,   101.78",
                "91,   103.53",
                "365,  104.73",
                "1000, 107.51"
        })
        void importoPerGiorni(long giorni, String atteso) {
            assertThat(strategia.calcolaImporto(tari("100.00"), giorni)).isEqualTo(new BigDecimal(atteso));
        }

        @Test
        @DisplayName("importo con centesimi: arrotondamento HALF_UP a due decimali")
        void arrotondamento() {
            // sanzione 0.2776 -> 0.28, interessi 0.0146 -> 0.01: 33.33 + 0.28 + 0.01 = 33.62
            assertThat(strategia.calcolaImporto(tari("33.33"), 10)).isEqualTo(new BigDecimal("33.62"));
        }

        @Test
        @DisplayName("il risultato ha sempre due decimali")
        void scalaDue() {
            assertThat(strategia.calcolaImporto(tari("100"), 20).scale()).isEqualTo(2);
        }
    }


    @Nested
    @DisplayName("Base di calcolo e parametri")
    class Parametri {

        @Test
        @DisplayName("si basa sull'importo originale, non su quello attuale")
        void baseImportoOriginale() {
            Posizione p = tari("100.00");
            p.setImporto(new BigDecimal("999.00"));

            assertThat(strategia.calcolaImporto(p, 10)).isEqualTo(new BigDecimal("100.87"));
        }

        @Test
        @DisplayName("tasso legale diverso: cambiano gli interessi")
        void tassoConfigurabile() {
            props.setTassoLegale(new BigDecimal("2.50"));

            assertThat(strategia.calcolaImporto(tari("100.00"), 365)).isEqualTo(new BigDecimal("105.63"));
        }

        @Test
        @DisplayName("tasso legale zero: solo la sanzione")
        void tassoZero() {
            props.setTassoLegale(BigDecimal.ZERO);

            assertThat(strategia.calcolaImporto(tari("100.00"), 91)).isEqualTo(new BigDecimal("103.13"));
        }

        @Test
        @DisplayName("il tasso viene letto a ogni calcolo, non alla costruzione")
        void letturaAOgniCalcolo() {
            BigDecimal prima = strategia.calcolaImporto(tari("100.00"), 365);

            props.setTassoLegale(new BigDecimal("2.50"));

            assertThat(prima).isEqualTo(new BigDecimal("104.73"));
            assertThat(strategia.calcolaImporto(tari("100.00"), 365)).isEqualTo(new BigDecimal("105.63"));
        }
    }


    @Nested
    @DisplayName("Casi limite")
    class CasiLimite {

        @Test
        @DisplayName("giorni negativi: importo ridotto (sanzione e interessi negativi, nessun controllo)")
        void giorniNegativi() {
            assertThat(strategia.calcolaImporto(tari("100.00"), -5)).isEqualTo(new BigDecimal("99.56"));
        }

        @Test
        @DisplayName("importo originale zero: 0.00")
        void originaleZero() {
            assertThat(strategia.calcolaImporto(tari("0"), 100)).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("importo originale null: NullPointerException")
        void originaleNull() {
            Posizione p = tari("100.00");
            p.setImportoOriginale(null);

            assertThatThrownBy(() -> strategia.calcolaImporto(p, 10))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("tasso legale non configurato (null): NullPointerException")
        void tassoNull() {
            props.setTassoLegale(null);

            Posizione p = tari("100.00");

            assertThatThrownBy(() -> strategia.calcolaImporto(p, 10))
                    .isInstanceOf(NullPointerException.class);
        }
    }


    private static Posizione tari(String importoOriginale) {
        Posizione p = new Posizione();
        p.setIuv("IUV-TARI");
        p.setTipoTributo("TARI");
        p.setImporto(new BigDecimal(importoOriginale));
        p.setImportoOriginale(new BigDecimal(importoOriginale));
        return p;
    }
}