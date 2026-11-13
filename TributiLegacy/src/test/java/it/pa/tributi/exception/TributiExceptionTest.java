package it.pa.tributi.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TributiException")
class TributiExceptionTest {

    @Test
    @DisplayName("costruttore con codice e messaggio: li conserva, causa null")
    void codiceEMessaggio() {
        TributiException ex = new TributiException(
                CodiciErrore.CODICE_IUV_PAGAMENTO_NOT_FOUND,
                CodiciErrore.MESSAGGIO_IUV_PAGAMENTO_NOT_FOUND);

        assertEquals("NIUV_001", ex.getCodice());
        assertEquals("Identificativo versamento non trovato", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("costruttore con causa: conserva codice, messaggio e causa")
    void conCausa() {
        IllegalStateException causa = new IllegalStateException("EntityManager chiuso");

        TributiException ex = new TributiException(
                CodiciErrore.CODICE_ERRORE_INTERNO,
                CodiciErrore.MESSAGGIO_ERRORE_INTERNO,
                causa);

        assertEquals("INT_001", ex.getCodice());
        assertEquals("Errore interno", ex.getMessage());
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("il messaggio non include quello della causa")
    void messaggioSenzaCausa() {
        TributiException ex = new TributiException("INT_001", "Errore interno",
                new RuntimeException("dettaglio tecnico"));

        assertEquals("Errore interno", ex.getMessage());
    }

    @Test
    @DisplayName("accetta codice, messaggio e causa null")
    void valoriNull() {
        TributiException ex = new TributiException(null, null, null);

        assertNull(ex.getCodice());
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("è un'eccezione controllata: estende Exception ma non RuntimeException")
    void controllata() {
        assertTrue(Exception.class.isAssignableFrom(TributiException.class));
        assertFalse(RuntimeException.class.isAssignableFrom(TributiException.class));
    }

    @Test
    @DisplayName("il codice è final e non ha un setter")
    void codiceImmutabile() throws Exception {
        Field codice = TributiException.class.getDeclaredField("codice");

        assertTrue(Modifier.isFinal(codice.getModifiers()));
        assertThrows(NoSuchMethodException.class,
                () -> TributiException.class.getMethod("setCodice", String.class));
    }

    @Test
    @DisplayName("ha due costruttori: (codice, messaggio) e (codice, messaggio, causa)")
    void costruttori() throws Exception {
        assertEquals(2, TributiException.class.getConstructors().length);
        assertNotNull(TributiException.class.getConstructor(String.class, String.class));
        assertNotNull(TributiException.class.getConstructor(String.class, String.class, Throwable.class));
    }
}