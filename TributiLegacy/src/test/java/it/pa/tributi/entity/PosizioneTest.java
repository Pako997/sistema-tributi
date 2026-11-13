package it.pa.tributi.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Posizione - mapping JPA")
class PosizioneTest {

    private static final long GIORNO = 24L * 60 * 60 * 1000;

    private static EntityManagerFactory emf;

    private EntityManager em;
    private Ente ente;

    @BeforeAll
    static void creaFactory() {
        emf = Persistence.createEntityManagerFactory("tributiTestPU");
    }

    @AfterAll
    static void chiudiFactory() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void apri() {
        em = emf.createEntityManager();
        em.getTransaction().begin();

        ente = new Ente();
        ente.setCodiceEnte("C_F839");
        ente.setDenominazione("Comune di Napoli");
        ente.setContatto("tributi@comune.napoli.it");
        em.persist(ente);
        em.flush();
    }

    @AfterEach
    void annulla() {
        if (em == null) {
            return;
        }
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }


    @Test
    @DisplayName("una Posizione nuova ha tutti i campi null")
    void campiInizialiNull() {
        Posizione p = new Posizione();

        assertNull(p.getId());
        assertNull(p.getIuv());
        assertNull(p.getCodiceFiscale());
        assertNull(p.getTipoTributo());
        assertNull(p.getImporto());
        assertNull(p.getImportoOriginale());
        assertNull(p.getStato());
        assertNull(p.getDataEmissione());
        assertNull(p.getDataScadenza());
        assertNull(p.getDataPagamento());
        assertNull(p.getEnte());
    }

    @Test
    @DisplayName("ogni setter valorizza il proprio campo")
    void getterSetter() {
        Date emissione = new Date(1700000000000L);
        Date scadenza = new Date(1702592000000L);
        Date pagamento = new Date(1701000000000L);
        Posizione p = posizione("IUV-1");
        p.setId(7L);
        p.setImporto(new BigDecimal("130.00"));
        p.setStato("PAGATA");
        p.setDataEmissione(emissione);
        p.setDataScadenza(scadenza);
        p.setDataPagamento(pagamento);

        assertEquals(Long.valueOf(7L), p.getId());
        assertEquals("IUV-1", p.getIuv());
        assertEquals("RSSMRA80A01F839X", p.getCodiceFiscale());
        assertEquals("TARI", p.getTipoTributo());
        assertEquals(new BigDecimal("130.00"), p.getImporto());
        assertEquals(new BigDecimal("100.00"), p.getImportoOriginale());
        assertEquals("PAGATA", p.getStato());
        assertEquals(emissione, p.getDataEmissione());
        assertEquals(scadenza, p.getDataScadenza());
        assertEquals(pagamento, p.getDataPagamento());
        assertSame(ente, p.getEnte());
    }


    @Nested
    @DisplayName("Salvataggio e rilettura")
    class Salvataggio {

        @Test
        @DisplayName("l'id viene generato dal database")
        void idGenerato() {
            Posizione p = posizione("IUV-1");

            salva(p);

            assertNotNull(p.getId());
        }

        @Test
        @DisplayName("rilettura: tutti i campi vengono conservati, ente compreso")
        void rilettura() {
            Date pagamento = new Date(System.currentTimeMillis() - GIORNO);
            Posizione p = posizione("IUV-1");
            p.setStato("PAGATA");
            p.setDataPagamento(pagamento);
            salva(p);
            em.clear();

            Posizione letta = em.find(Posizione.class, p.getId());

            assertEquals("IUV-1", letta.getIuv());
            assertEquals("RSSMRA80A01F839X", letta.getCodiceFiscale());
            assertEquals("TARI", letta.getTipoTributo());
            assertEquals(0, new BigDecimal("100.00").compareTo(letta.getImporto()));
            assertEquals(0, new BigDecimal("100.00").compareTo(letta.getImportoOriginale()));
            assertEquals("PAGATA", letta.getStato());
            assertEquals(giorno(p.getDataEmissione()), giorno(letta.getDataEmissione()));
            assertEquals(giorno(p.getDataScadenza()), giorno(letta.getDataScadenza()));
            assertEquals(giorno(pagamento), giorno(letta.getDataPagamento()));
            assertEquals("C_F839", letta.getEnte().getCodiceEnte());
            assertEquals("Comune di Napoli", letta.getEnte().getDenominazione());
        }

        @Test
        @DisplayName("importo con un solo decimale: riletto con due decimali")
        void scalaDueDecimali() {
            Posizione p = posizione("IUV-1");
            p.setImporto(new BigDecimal("100.5"));
            salva(p);
            em.clear();

            assertEquals(new BigDecimal("100.50"), em.find(Posizione.class, p.getId()).getImporto());
        }

        @Test
        @DisplayName("date di tipo DATE: l'ora viene persa, si rilegge la mezzanotte")
        void oraPersa() {
            Calendar pomeriggio = Calendar.getInstance();
            pomeriggio.set(2026, Calendar.MARCH, 10, 15, 45, 30);
            Posizione p = posizione("IUV-1");
            p.setDataScadenza(pomeriggio.getTime());
            salva(p);
            em.clear();

            Calendar letta = Calendar.getInstance();
            letta.setTime(em.find(Posizione.class, p.getId()).getDataScadenza());

            assertEquals("2026-03-10", giorno(letta.getTime()));
            assertEquals(0, letta.get(Calendar.HOUR_OF_DAY));
            assertEquals(0, letta.get(Calendar.MINUTE));
        }

        @Test
        @DisplayName("data di pagamento null: accettata")
        void dataPagamentoNull() {
            Posizione p = posizione("IUV-1");

            salva(p);
            em.clear();

            assertNull(em.find(Posizione.class, p.getId()).getDataPagamento());
        }

        @Test
        @DisplayName("più posizioni sullo stesso ente: accettate")
        void stessoEnte() {
            salva(posizione("IUV-1"));
            salva(posizione("IUV-2"));

            Long totale = em.createQuery(
                            "SELECT COUNT(p) FROM Posizione p WHERE p.ente.codiceEnte = 'C_F839'", Long.class)
                    .getSingleResult();
            assertEquals(2L, totale.longValue());
        }
    }

    @Nested
    @DisplayName("Vincoli")
    class Vincoli {

        @ParameterizedTest(name = "{0} null: rifiutato")
        @ValueSource(strings = {
                "iuv", "codiceFiscale", "tipoTributo", "importo", "importoOriginale",
                "stato", "dataEmissione", "dataScadenza"
        })
        void campoObbligatorioNull(String campo) {
            Posizione p = posizione("IUV-1");
            azzera(p, campo);

            assertThrows(PersistenceException.class, () -> salva(p));
        }

        @ParameterizedTest(name = "{0} oltre {1} caratteri: rifiutato")
        @CsvSource({
                "iuv,           35",
                "codiceFiscale, 16",
                "tipoTributo,   50",
                "stato,         20"
        })
        void testoTroppoLungo(String campo, int massimo) {
            Posizione p = posizione("IUV-1");
            imposta(p, campo, ripeti('X', massimo + 1));

            assertThrows(PersistenceException.class, () -> salva(p));
        }

        @ParameterizedTest(name = "{0} di esattamente {1} caratteri: accettato")
        @CsvSource({
                "iuv,           35",
                "codiceFiscale, 16",
                "tipoTributo,   50",
                "stato,         20"
        })
        void testoAlLimite(String campo, int massimo) {
            Posizione p = posizione("IUV-1");
            imposta(p, campo, ripeti('X', massimo));

            salva(p);

            assertNotNull(p.getId());
        }

        @Test
        @DisplayName("iuv duplicato: rifiutato")
        void iuvDuplicato() {
            salva(posizione("IUV-1"));

            assertThrows(PersistenceException.class, () -> salva(posizione("IUV-1")));
        }

        @Test
        @DisplayName("importo massimo con precisione 10 e scala 2 (99999999.99): accettato")
        void importoMassimo() {
            Posizione p = posizione("IUV-1");
            p.setImporto(new BigDecimal("99999999.99"));

            salva(p);

            assertNotNull(p.getId());
        }

        @Test
        @DisplayName("importo oltre la precisione (100000000.00): rifiutato")
        void importoTroppoGrande() {
            Posizione p = posizione("IUV-1");
            p.setImporto(new BigDecimal("100000000.00"));

            assertThrows(PersistenceException.class, () -> salva(p));
        }

        @Test
        @DisplayName("ente null: accettato (la relazione non è dichiarata obbligatoria)")
        void enteNull() {
            Posizione p = posizione("IUV-1");
            p.setEnte(null);

            salva(p);

            assertNotNull(p.getId());
        }

        @Test
        @DisplayName("ente non salvato: rifiutato")
        void enteNonSalvato() {
            Ente nuovo = new Ente();
            nuovo.setCodiceEnte("C_NUOVO");
            nuovo.setDenominazione("Comune non salvato");
            Posizione p = posizione("IUV-1");
            p.setEnte(nuovo);

            assertThrows(RuntimeException.class, () -> salva(p));
        }

        @Test
        @DisplayName("cancellare un ente che ha posizioni: rifiutato dalla chiave esterna")
        void cancellaEnteConPosizioni() {
            salva(posizione("IUV-1"));

            assertThrows(PersistenceException.class, () -> {
                em.remove(ente);
                em.flush();
            });
        }
    }


    private void salva(Posizione p) {
        em.persist(p);
        em.flush();
    }

    private Posizione posizione(String iuv) {
        Posizione p = new Posizione();
        p.setIuv(iuv);
        p.setCodiceFiscale("RSSMRA80A01F839X");
        p.setTipoTributo("TARI");
        p.setImporto(new BigDecimal("100.00"));
        p.setImportoOriginale(new BigDecimal("100.00"));
        p.setStato("DA_PAGARE");
        p.setDataEmissione(new Date());
        p.setDataScadenza(new Date(System.currentTimeMillis() + 30 * GIORNO));
        p.setEnte(ente);
        return p;
    }

    private static void azzera(Posizione p, String campo) {
        switch (campo) {
            case "iuv": p.setIuv(null); break;
            case "codiceFiscale": p.setCodiceFiscale(null); break;
            case "tipoTributo": p.setTipoTributo(null); break;
            case "importo": p.setImporto(null); break;
            case "importoOriginale": p.setImportoOriginale(null); break;
            case "stato": p.setStato(null); break;
            case "dataEmissione": p.setDataEmissione(null); break;
            case "dataScadenza": p.setDataScadenza(null); break;
            default: throw new IllegalArgumentException(campo);
        }
    }

    private static void imposta(Posizione p, String campo, String valore) {
        switch (campo) {
            case "iuv": p.setIuv(valore); break;
            case "codiceFiscale": p.setCodiceFiscale(valore); break;
            case "tipoTributo": p.setTipoTributo(valore); break;
            case "stato": p.setStato(valore); break;
            default: throw new IllegalArgumentException(campo);
        }
    }

    private static String giorno(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd").format(data);
    }

    private static String ripeti(char c, int volte) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < volte; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}