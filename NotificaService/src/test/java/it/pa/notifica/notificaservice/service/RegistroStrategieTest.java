package it.pa.notifica.notificaservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.pa.notifica.notificaservice.entity.Posizione;
import it.pa.notifica.notificaservice.strategy.StrategiaRivalutazione;

@DisplayName("RegistroStrategie")
class RegistroStrategieTest {

    private final StrategiaRivalutazione multa = new StrategiaFinta("MULTA");
    private final StrategiaRivalutazione tari = new StrategiaFinta("TARI");

    @Test
    @DisplayName("restituisce la strategia registrata per il tributo")
    void strategiaRegistrata() {
        RegistroStrategie registro = new RegistroStrategie(List.of(multa, tari));

        assertThat(registro.per("MULTA")).containsSame(multa);
        assertThat(registro.per("TARI")).containsSame(tari);
    }

    @Test
    @DisplayName("tributo senza strategia: vuoto")
    void tributoSconosciuto() {
        RegistroStrategie registro = new RegistroStrategie(List.of(multa, tari));

        assertThat(registro.per("IMU")).isEmpty();
    }

    @Test
    @DisplayName("il nome del tributo distingue maiuscole e minuscole")
    void maiuscoleMinuscole() {
        RegistroStrategie registro = new RegistroStrategie(List.of(multa));

        assertThat(registro.per("multa")).isEmpty();
    }

    @Test
    @DisplayName("tributo null: vuoto")
    void tributoNull() {
        RegistroStrategie registro = new RegistroStrategie(List.of(multa, tari));

        assertThat(registro.per(null)).isEmpty();
    }

    @Test
    @DisplayName("nessuna strategia disponibile: sempre vuoto")
    void registroVuoto() {
        RegistroStrategie registro = new RegistroStrategie(List.of());

        assertThat(registro.per("MULTA")).isEmpty();
        assertThat(registro.per("TARI")).isEmpty();
    }

    @Test
    @DisplayName("due strategie per lo stesso tributo: errore alla costruzione")
    void tributoDuplicato() {
        StrategiaRivalutazione altraMulta = new StrategiaFinta("MULTA");

        assertThatThrownBy(() -> new RegistroStrategie(List.of(multa, altraMulta)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MULTA");
    }

    @Test
    @DisplayName("modificare la lista dopo la costruzione non cambia il registro")
    void copiaDellaLista() {
        java.util.ArrayList<StrategiaRivalutazione> disponibili = new java.util.ArrayList<>(List.of(multa));
        RegistroStrategie registro = new RegistroStrategie(disponibili);

        disponibili.add(tari);

        assertThat(registro.per("TARI")).isEmpty();
    }


    private static final class StrategiaFinta implements StrategiaRivalutazione {
        private final String tipo;

        StrategiaFinta(String tipo) {
            this.tipo = tipo;
        }

        @Override
        public String tipoTributo() {
            return tipo;
        }

        @Override
        public BigDecimal calcolaImporto(Posizione posizione, long giorniRitardo) {
            return BigDecimal.ZERO;
        }
    }
}