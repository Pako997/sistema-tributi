package it.pa.tributi.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ServiceNotFoundException")
class ServiceNotFoundExceptionTest {

    @Test
    @DisplayName("conserva il messaggio")
    void messaggio() {
        ServiceNotFoundException ex = new ServiceNotFoundException("Non trovato: IServizio");

        assertEquals("Non trovato: IServizio", ex.getMessage());
    }

    @Test
    @DisplayName("non ha una causa: non c'è un costruttore che la accetti")
    void senzaCausa() {
        assertNull(new ServiceNotFoundException("Non trovato").getCause());
    }

    @Test
    @DisplayName("accetta un messaggio null")
    void messaggioNull() {
        assertNull(new ServiceNotFoundException(null).getMessage());
    }

    @Test
    @DisplayName("è una RuntimeException: non va dichiarata con throws")
    void nonControllata() {
        assertTrue(RuntimeException.class.isAssignableFrom(ServiceNotFoundException.class));

        ServiceNotFoundException ex = assertThrows(ServiceNotFoundException.class,
                ServiceNotFoundExceptionTest::lanciaSenzaThrows);
        assertEquals("lanciata", ex.getMessage());
    }

    @Test
    @DisplayName("ha un solo costruttore, con il messaggio")
    void unSoloCostruttore() throws Exception {
        assertEquals(1, ServiceNotFoundException.class.getConstructors().length);
        assertNotNull(ServiceNotFoundException.class.getConstructor(String.class));
    }

    // metodo senza "throws": compila solo perché l'eccezione non è controllata
    private static void lanciaSenzaThrows() {
        throw new ServiceNotFoundException("lanciata");
    }
}