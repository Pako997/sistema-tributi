package it.pa.tributi.external;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.pa.tributi.dto.PosizioneDTO;

@DisplayName("SistemaPostaleStub")
class SistemaPostaleStubTest {

    private static final Date SCADENZA = new Date(1702592000000L);

    private final SistemaPostaleStub sistema = new SistemaPostaleStub();

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
    @DisplayName("implementa ISistemaPostale")
    void implementaInterfaccia() {
        assertTrue(sistema instanceof ISistemaPostale);
    }

    @Test
    @DisplayName("una riga per posizione con IUV, codice fiscale e scadenza")
    void rigaPerPosizione() {
        sistema.inviaBlocco(Collections.singletonList(posizione("IUV-1", "RSSMRA80A01F839X")));

         assertEquals("[POSTA] Invio avviso IUV-1 a RSSMRA80A01F839X - scadenza " + SCADENZA,
                righe().get(0));
    }

    @Test
    @DisplayName("blocco di più posizioni: una riga ciascuna, nello stesso ordine")
    void bloccoMultiplo() {
        sistema.inviaBlocco(Arrays.asList(
                posizione("IUV-1", "RSSMRA80A01F839X"),
                posizione("IUV-2", "VRDLGI75B02H501Y"),
                posizione("IUV-3", "BNCGNN90C03F205Z")));

        List<String> righe = righe();
        assertEquals(3, righe.size());
        assertTrue(righe.get(0).contains("IUV-1"));
        assertTrue(righe.get(1).contains("IUV-2"));
        assertTrue(righe.get(2).contains("IUV-3"));
    }

    @Test
    @DisplayName("blocco vuoto: non stampa nulla")
    void bloccoVuoto() {
        sistema.inviaBlocco(Collections.<PosizioneDTO>emptyList());

        assertTrue(console.toString().isEmpty());
    }

    @Test
    @DisplayName("campi null: stampati come 'null', nessun errore")
    void campiNull() {
        sistema.inviaBlocco(Collections.singletonList(new PosizioneDTO()));

        assertEquals("[POSTA] Invio avviso null a null - scadenza null", righe().get(0));
    }

    @Test
    @DisplayName("blocco null: NullPointerException")
    void bloccoNull() {
        assertThrows(NullPointerException.class, () -> sistema.inviaBlocco(null));
    }

    @Test
    @DisplayName("posizione null nel blocco: NullPointerException dopo aver già inviato le precedenti")
    void posizioneNullNelBlocco() {
        List<PosizioneDTO> blocco = new ArrayList<>();
        blocco.add(posizione("IUV-1", "RSSMRA80A01F839X"));
        blocco.add(null);
        blocco.add(posizione("IUV-3", "BNCGNN90C03F205Z"));

        assertThrows(NullPointerException.class, () -> sistema.inviaBlocco(blocco));

        List<String> righe = righe();
        assertEquals(1, righe.size());
        assertTrue(righe.get(0).contains("IUV-1"));
    }

    private List<String> righe() {
        List<String> righe = new ArrayList<>();
        for (String r : console.toString().split("\\r?\\n")) {
            if (!r.isEmpty()) {
                righe.add(r);
            }
        }
        return righe;
    }

    private static PosizioneDTO posizione(String iuv, String codiceFiscale) {
        PosizioneDTO p = new PosizioneDTO();
        p.setIuv(iuv);
        p.setCodiceFiscale(codiceFiscale);
        p.setDataScadenza(SCADENZA);
        return p;
    }
}