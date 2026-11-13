package it.pa.tributi.tributiservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GeneratoreIuv")
class GeneratoreIuvTest {

    private final GeneratoreIuv generatore = new GeneratoreIuv();

    @Test
    @DisplayName("lo IUV è composto da 17 cifre")
    void diciassetteCifre() {
        assertThat(generatore.genera()).matches("\\d{17}");
    }

    @Test
    @DisplayName("le prime 13 cifre sono i millisecondi del momento della generazione")
    void prefissoTimestamp() {
        long prima = System.currentTimeMillis();

        String iuv = generatore.genera();

        long dopo = System.currentTimeMillis();
        long timestamp = Long.parseLong(iuv.substring(0, 13));
        assertThat(timestamp).isBetween(prima, dopo);
    }

    @Test
    @DisplayName("la parte casuale ha sempre 4 cifre, con gli zeri iniziali quando serve")
    void parteCasualeQuattroCifre() {
        boolean trovatoZeroIniziale = false;
        for (int i = 0; i < 5000; i++) {
            String iuv = generatore.genera();
            assertThat(iuv).hasSize(17);
            if (iuv.charAt(13) == '0') {
                trovatoZeroIniziale = true;
            }
        }

        assertThat(trovatoZeroIniziale).isTrue();
    }

    @Test
    @DisplayName("la parte casuale cambia tra una generazione e l'altra")
    void parteCasualeVariabile() {
        Set<String> suffissi = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            suffissi.add(generatore.genera().substring(13));
        }

        assertThat(suffissi).hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("lo IUV entra nella colonna iuv (massimo 35 caratteri)")
    void entraNellaColonna() {
        assertThat(generatore.genera().length()).isLessThanOrEqualTo(35);
    }

    @Test
    @DisplayName("IUV generati in momenti successivi sono in ordine crescente")
    void ordineCrescente() {
        String primo = generatore.genera();
        long millisecondoPrimo = Long.parseLong(primo.substring(0, 13));
        while (System.currentTimeMillis() <= millisecondoPrimo) {
            Thread.onSpinWait();
        }
        String secondo = generatore.genera();

        assertThat(secondo).isGreaterThan(primo);
    }
}