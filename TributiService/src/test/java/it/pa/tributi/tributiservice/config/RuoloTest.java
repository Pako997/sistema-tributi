package it.pa.tributi.tributiservice.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Ruolo")
class RuoloTest {

    @Test
    @DisplayName("esistono esattamente i ruoli CITTADINO e OPERATORE")
    void ruoli() {
        assertThat(Ruolo.values()).containsExactly(Ruolo.CITTADINO, Ruolo.OPERATORE);
    }

    @Test
    @DisplayName("con il prefisso ROLE_ il nome coincide con l'autorità controllata dal controller")
    void nomeAutorita() {
        assertThat("ROLE_" + Ruolo.OPERATORE.name()).isEqualTo("ROLE_OPERATORE");
    }

    @Test
    @DisplayName("valueOf distingue maiuscole e minuscole")
    void valueOf() {
        assertThat(Ruolo.valueOf("OPERATORE")).isEqualTo(Ruolo.OPERATORE);
        assertThatThrownBy(() -> Ruolo.valueOf("operatore"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}