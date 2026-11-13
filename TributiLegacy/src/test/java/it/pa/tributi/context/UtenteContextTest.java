package it.pa.tributi.context;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UtenteContextTest {

    private static final String CF = "RSSMRA80A01F839X";

    @AfterEach
    void pulisci() {
        UtenteContext.clear();
    }

    @Test
    @DisplayName("senza set il codice fiscale è null")
    void valorePredefinitoNull() {
        assertNull(UtenteContext.getCodiceFiscale());
    }

    @Test
    @DisplayName("set e get sullo stesso thread")
    void setEGet() {
        UtenteContext.set(CF);

        assertEquals(CF, UtenteContext.getCodiceFiscale());
    }

    @Test
    @DisplayName("clear rimuove il valore")
    void clearRimuove() {
        UtenteContext.set(CF);
        UtenteContext.clear();

        assertNull(UtenteContext.getCodiceFiscale());
    }

    @Test
    @DisplayName("un secondo set sovrascrive il primo")
    void setSovrascrive() {
        UtenteContext.set(CF);
        UtenteContext.set("VRDLGI75B02H501Y");

        assertEquals("VRDLGI75B02H501Y", UtenteContext.getCodiceFiscale());
    }

    @Test
    @DisplayName("set accetta null senza errori")
    void setNull() {
        UtenteContext.set(CF);
        UtenteContext.set(null);

        assertNull(UtenteContext.getCodiceFiscale());
    }

    @Test
    @DisplayName("un altro thread non vede il valore (nemmeno un thread figlio)")
    void isolamentoTraThread() throws Exception {
        UtenteContext.set(CF);
        AtomicReference<String> vistoDalFiglio = new AtomicReference<>("non eseguito");

        Thread figlio = new Thread(() -> vistoDalFiglio.set(UtenteContext.getCodiceFiscale()));
        figlio.start();
        figlio.join();

        assertNull(vistoDalFiglio.get());
        assertEquals(CF, UtenteContext.getCodiceFiscale());
    }

    @Test
    @DisplayName("CARATTERIZZAZIONE: senza clear il valore resta sul thread riusato da un pool")
    void perditaSuThreadRiusato() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            pool.submit(() -> UtenteContext.set(CF)).get();

            String vistoDallaRichiesta2 = pool.submit(UtenteContext::getCodiceFiscale).get();

            assertEquals(CF, vistoDallaRichiesta2);
        } finally {
            pool.submit(UtenteContext::clear).get();
            pool.shutdown();
        }
    }
}