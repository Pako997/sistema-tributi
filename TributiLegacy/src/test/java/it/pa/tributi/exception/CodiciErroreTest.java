package it.pa.tributi.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("CodiciErrore")
class CodiciErroreTest {

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource({
            "CODICE_AUTENTICAZIONE_MANCANTE, AUTH_000",
            "CODICE_UTENTE_NON_AUTORIZZATO,  AUTH_001",
            "CODICE_ERRORE_INTERNO,          INT_001",
            "CODICE_FORBIDDEM,               FORB_001",
            "CODICE_NOTFOUND,                NFOU_001",
            "CODICE_PARAMETRI_MANCANTI,      PMISS_001",
            "CODICE_IMPORTO_NULLO,           IMPO_001",
            "CODICE_DATA_SFORMATA,           DATA_001",
            "CODICE_DATA_NON_PARSABILE,      DATA_002",
            "CODICE_SCADENZA_NON_VALIDA,     DATA_003",
            "CODICE_IUV_PAGAMENTO_NOT_FOUND, NIUV_001",
            "CODICE_PAGAMENTO_NON_RIUSCITO,  PERR_001"
    })
    void valoreCodice(String nome, String atteso) throws Exception {
        assertEquals(atteso, valore(nome));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', quoteCharacter = '"', value = { // '"' perché un messaggio contiene l'apostrofo
            "MESSAGGIO_AUTENTICAZIONE_MANCANTE | Informazioni di autenticazione mancanti",
            "MESSAGGIO_UTENTE_NON_AUTORIZZATO  | Utente non autorizzato",
            "MESSAGGIO_ERRORE_INTERNO          | Errore interno",
            "MESSAGGIO_FORBIDDEM               | L'operazione richiesta non è consentita",
            "MESSAGGIO_ENTE_NOTFOUND           | Ente non trovato",
            "MESSAGGIO_PARAMETRI_MANCANTI      | Uno o piu parametri forniti non sono stati valorizzati",
            "MESSAGGIO_IMPORTO_NULLO           | Importo nullo, impossibile procedere",
            "MESSAGGIO_DATA_SFORMATA           | La data fornita in input non segue il formato yyyy-MM-dd",
            "MESSAGGIO_DATA_NON_PARSABILE      | La data fornita in input non è valida",
            "MESSAGGIO_SCADENZA_NON_VALIDA     | La data scadenza deve essere almeno a 30 giorni dalla data emissione",
            "MESSAGGIO_IUV_PAGAMENTO_NOT_FOUND | Identificativo versamento non trovato",
            "MESSAGGIO_PAGAMENTO_NON_RIUSCITO  | Pagamento non riuscito. Verificare il metodo"
    })
    void valoreMessaggio(String nome, String atteso) throws Exception {
        assertEquals(atteso, valore(nome));
    }

    @Test
    @DisplayName("ci sono 12 codici e 12 messaggi")
    void conteggio() throws Exception {
        assertEquals(12, costanti("CODICE_").size());
        assertEquals(12, costanti("MESSAGGIO_").size());
    }

    @Test
    @DisplayName("tutte le costanti sono public static final String")
    void modificatori() {
        for (Field f : CodiciErrore.class.getDeclaredFields()) {
            if (f.isSynthetic()) {
                continue;
            }
            int m = f.getModifiers();
            assertTrue(Modifier.isPublic(m), f.getName());
            assertTrue(Modifier.isStatic(m), f.getName());
            assertTrue(Modifier.isFinal(m), f.getName());
            assertEquals(String.class, f.getType(), f.getName());
        }
    }

    @Test
    @DisplayName("i codici sono tutti diversi tra loro")
    void codiciUnici() throws Exception {
        List<String> codici = new ArrayList<>(costanti("CODICE_").values());

        assertEquals(codici.size(), new HashSet<>(codici).size());
    }

    @Test
    @DisplayName("i codici seguono il formato PREFISSO_NNN")
    void formatoCodici() throws Exception {
        for (Map.Entry<String, String> c : costanti("CODICE_").entrySet()) {
            assertTrue(c.getValue().matches("[A-Z]+_\\d{3}"), c.getKey() + " = " + c.getValue());
        }
    }

    @Test
    @DisplayName("i messaggi non sono vuoti")
    void messaggiNonVuoti() throws Exception {
        for (Map.Entry<String, String> m : costanti("MESSAGGIO_").entrySet()) {
            assertFalse(m.getValue().trim().isEmpty(), m.getKey());
        }
    }

    @Test
    @DisplayName("ogni codice ha il messaggio con lo stesso nome, tranne CODICE_NOTFOUND (abbinato a MESSAGGIO_ENTE_NOTFOUND)")
    void abbinamentoCodiceMessaggio() throws Exception {
        Set<String> nomiMessaggi = costanti("MESSAGGIO_").keySet();
        List<String> senzaMessaggio = new ArrayList<>();
        for (String codice : costanti("CODICE_").keySet()) {
            String messaggio = "MESSAGGIO_" + codice.substring("CODICE_".length());
            if (!nomiMessaggi.contains(messaggio)) {
                senzaMessaggio.add(codice);
            }
        }

        assertEquals(Collections.singletonList("CODICE_NOTFOUND"), senzaMessaggio);
    }

    @Test
    @DisplayName("la classe si può istanziare (non ha un costruttore privato)")
    void istanziabile() {
        assertNotNull(new CodiciErrore());
    }

    private static String valore(String nomeCampo) throws Exception {
        return (String) CodiciErrore.class.getField(nomeCampo).get(null);
    }

    private static Map<String, String> costanti(String prefisso) throws Exception {
        Map<String, String> risultato = new LinkedHashMap<>();
        for (Field f : CodiciErrore.class.getDeclaredFields()) {
            if (f.getName().startsWith(prefisso)) {
                risultato.put(f.getName(), (String) f.get(null));
            }
        }
        return risultato;
    }
}