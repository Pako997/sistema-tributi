package it.pa.tributi.external;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.pa.tributi.dto.PosizioneDTO;

@DisplayName("SistemaPagamentoStub")
class SistemaPagamentoStubTest {

    private final SistemaPagamentoStub sistema = new SistemaPagamentoStub();

    private PrintStream consoleOriginale;
    private ByteArrayOutputStream console;

    @BeforeEach
    void catturaConsole() {
        consoleOriginale = System.out;
        console = new ByteArrayOutputStream();
        System.setOut(new PrintStream(console, true));
    }

    @AfterEach
    void ripristinaConsole() {
        System.setOut(consoleOriginale);
    }

    @Test
    @DisplayName("implementa ISistemaPagamento")
    void implementaInterfaccia() {
        assertTrue(sistema instanceof ISistemaPagamento);
    }

    @Test
    @DisplayName("restituisce un id transazione di 32 caratteri esadecimali minuscoli")
    void formatoIdTransazione() {
        String id = sistema.verificaPagamento(posizione("IUV-1"), 100.0);

        assertEquals(32, id.length());
        assertTrue(id.matches("[0-9a-f]{32}"), id);
    }

    @Test
    @DisplayName("l'id transazione entra nella colonna id_transazione (massimo 35 caratteri)")
    void entraNellArchivio() {
        String id = sistema.verificaPagamento(posizione("IUV-1"), 100.0);

        assertTrue(id.length() <= 35);
    }

    @Test
    @DisplayName("ogni chiamata restituisce un id diverso, anche per lo stesso IUV")
    void idSempreDiversi() {
        Set<String> id = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            id.add(sistema.verificaPagamento(posizione("IUV-1"), 100.0));
        }

        assertEquals(1000, id.size());
    }

    @Test
    @DisplayName("scrive su console IUV e importo")
    void scriveSuConsole() {
        sistema.verificaPagamento(posizione("IUV-1"), 130.5);

        assertEquals("[PAGAMENTO] Pagamento di IUV-1 versati: 130.5", console.toString().trim());
    }

    @Test
    @DisplayName("non controlla l'importo: accetta zero, negativo e null")
    void importoNonControllato() {
        assertNotNull(sistema.verificaPagamento(posizione("IUV-1"), 0.0));
        assertNotNull(sistema.verificaPagamento(posizione("IUV-2"), -50.0));
        assertNotNull(sistema.verificaPagamento(posizione("IUV-3"), null));

        assertTrue(console.toString().contains("versati: null"));
    }

    @Test
    @DisplayName("non controlla la posizione: IUV null accettato")
    void iuvNull() {
        assertNotNull(sistema.verificaPagamento(posizione(null), 100.0));

        assertTrue(console.toString().contains("Pagamento di null"));
    }

    @Test
    @DisplayName("posizione null: NullPointerException")
    void posizioneNull() {
        assertThrows(NullPointerException.class, () -> sistema.verificaPagamento(null, 100.0));
    }


    private static PosizioneDTO posizione(String iuv) {
        PosizioneDTO p = new PosizioneDTO();
        p.setIuv(iuv);
        return p;
    }
}