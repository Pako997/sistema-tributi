package it.pa.tributi.tributiservice.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TributiException")
class TributiExceptionTest {

    @Test
    @DisplayName("conserva l'errore e usa il suo messaggio, senza causa")
    void soloErrore() {
        TributiException ex = new TributiException(ErroreTributi.IUV_NON_TROVATO);

        assertThat(ex.getErrore()).isEqualTo(ErroreTributi.IUV_NON_TROVATO);
        assertThat(ex.getMessage()).isEqualTo("Identificativo versamento non trovato");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("con causa: conserva errore, messaggio e causa")
    void conCausa() {
        IllegalStateException causa = new IllegalStateException("db non raggiungibile");

        TributiException ex = new TributiException(ErroreTributi.ERRORE_INTERNO, causa);

        assertThat(ex.getErrore()).isEqualTo(ErroreTributi.ERRORE_INTERNO);
        assertThat(ex.getMessage()).isEqualTo("Errore interno");
        assertThat(ex.getCause()).isSameAs(causa);
    }

    @Test
    @DisplayName("il messaggio non include quello della causa")
    void messaggioSenzaCausa() {
        TributiException ex = new TributiException(ErroreTributi.ERRORE_INTERNO,
                new RuntimeException("dettaglio tecnico"));

        assertThat(ex.getMessage()).doesNotContain("dettaglio tecnico");
    }

    @Test
    @DisplayName("è una RuntimeException: non va dichiarata con throws")
    void nonControllata() {
        assertThat(new TributiException(ErroreTributi.ERRORE_INTERNO)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("errore null: NullPointerException già nel costruttore")
    void erroreNull() {
        assertThatThrownBy(() -> new TributiException(null))
                .isInstanceOf(NullPointerException.class);
    }
}