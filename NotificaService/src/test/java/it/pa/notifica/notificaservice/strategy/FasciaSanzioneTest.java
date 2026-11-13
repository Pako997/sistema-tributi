package it.pa.notifica.notificaservice.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("FasciaSanzione")
class FasciaSanzioneTest {

    private static final BigDecimal CENTO = new BigDecimal("100.00");


    @Nested
    @DisplayName("perGiorni")
    class PerGiorni {

        @ParameterizedTest(name = "{0} giorni -> {1}")
        @CsvSource({
                "-5,   SPRINT",
                "0,    SPRINT",
                "1,    SPRINT",
                "14,   SPRINT",
                "15,   BREVE",
                "30,   BREVE",
                "31,   INTERMEDIA",
                "90,   INTERMEDIA",
                "91,   LUNGA",
                "3650, LUNGA"
        })
        void fasciaPerGiorni(long giorni, FasciaSanzione attesa) {
            assertThat(FasciaSanzione.perGiorni(giorni)).isEqualTo(attesa);
        }

        @Test
        @DisplayName("Long.MAX_VALUE giorni -> LUNGA")
        void massimo() {
            assertThat(FasciaSanzione.perGiorni(Long.MAX_VALUE)).isEqualTo(FasciaSanzione.LUNGA);
        }

        @Test
        @DisplayName("le fasce sono dichiarate dalla più breve alla più lunga (perGiorni dipende dall'ordine)")
        void ordineFasce() {
            assertThat(FasciaSanzione.values()).containsExactly(
                    FasciaSanzione.SPRINT, FasciaSanzione.BREVE,
                    FasciaSanzione.INTERMEDIA, FasciaSanzione.LUNGA);
        }
    }


    @Nested
    @DisplayName("calcola")
    class Calcola {

        @ParameterizedTest(name = "SPRINT, {0} giorni su 100.00 -> {1}")
        @CsvSource({
                "0,  0.00",
                "1,  0.08",   // 0.0833
                "6,  0.50",   // 0.4998
                "10, 0.83",   // 0.833
                "14, 1.17"    // 1.1662
        })
        void sprintProporzionaleAiGiorni(long giorni, String attesa) {
            assertThat(FasciaSanzione.SPRINT.calcola(CENTO, giorni)).isEqualTo(new BigDecimal(attesa));
        }

        @ParameterizedTest(name = "{0} su 100.00 -> {1}")
        @CsvSource({
                "BREVE,      1.25",
                "INTERMEDIA, 1.39",
                "LUNGA,      3.13"    // 3.125 arrotondato HALF_UP
        })
        void fasceAPercentualeFissa(FasciaSanzione fascia, String attesa) {
            assertThat(fascia.calcola(CENTO, 20)).isEqualTo(new BigDecimal(attesa));
        }

        @Test
        @DisplayName("fasce non SPRINT: i giorni non cambiano la sanzione")
        void giorniIgnorati() {
            assertThat(FasciaSanzione.BREVE.calcola(CENTO, 16))
                    .isEqualTo(FasciaSanzione.BREVE.calcola(CENTO, 30));
            assertThat(FasciaSanzione.LUNGA.calcola(CENTO, 100))
                    .isEqualTo(FasciaSanzione.LUNGA.calcola(CENTO, 1000));
        }

        @Test
        @DisplayName("il risultato ha sempre due decimali")
        void scalaDue() {
            assertThat(FasciaSanzione.BREVE.calcola(new BigDecimal("80"), 20).scale()).isEqualTo(2);
            assertThat(FasciaSanzione.SPRINT.calcola(new BigDecimal("1234.5678"), 3).scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("importo zero: sanzione 0.00")
        void importoZero() {
            assertThat(FasciaSanzione.LUNGA.calcola(BigDecimal.ZERO, 200)).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("SPRINT con giorni negativi: sanzione negativa (nessun controllo)")
        void giorniNegativi() {
            assertThat(FasciaSanzione.SPRINT.calcola(CENTO, -5)).isEqualTo(new BigDecimal("-0.42"));
        }

        @Test
        @DisplayName("importo null: NullPointerException")
        void importoNull() {
            assertThatThrownBy(() -> FasciaSanzione.BREVE.calcola(null, 20))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}