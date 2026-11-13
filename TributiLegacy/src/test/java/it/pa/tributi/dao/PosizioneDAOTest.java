package it.pa.tributi.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TransactionRequiredException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.entity.Ente;
import it.pa.tributi.entity.Posizione;
import it.pa.tributi.exception.TributiException;

@DisplayName("PosizioneDAO - test d'integrazione")
class PosizioneDAOTest {

    private static final long GIORNO = 24L * 60 * 60 * 1000;
    private static final String CF = "RSSMRA80A01F839X";

    private static EntityManagerFactory emf;

    private EntityManager em;
    private PosizioneDAO dao;
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
    void preparaDao() throws Exception {
        em = emf.createEntityManager();
        dao = new PosizioneDAO();
        inietta(dao, em);
        em.getTransaction().begin();

        ente = new Ente();
        ente.setCodiceEnte("C_F839");
        ente.setDenominazione("Comune di Napoli");
        ente.setContatto("tributi@comune.napoli.it");
    }

    @AfterEach
    void annulla() {
        if (em == null) {
            return;
        }
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback(); // nessun dato resta nel DB tra un test e l'altro
        }
        em.close();
    }

    // ------------------------------------------------------------------
    // aggiornaPosizioneInCorso
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("aggiornaPosizioneInCorso")
    class InCorso {

        @Test
        @DisplayName("imposta lo stato IN_CORSO e lo scrive sul DB anche senza merge")
        void impostaInCorso() throws Exception {
            salva(ente, posizione("IUV-1", "DA_PAGARE"));

            dao.aggiornaPosizioneInCorso("IUV-1");
            em.flush();
            em.clear();

            assertEquals("IN_CORSO", rileggi("IUV-1").getStato());
        }

        @Test
        @DisplayName("IUV inesistente: TributiException del DAO, senza causa tecnica")
        void iuvInesistente() {
            TributiException ex = assertThrows(TributiException.class,
                    () -> dao.aggiornaPosizioneInCorso("NON_ESISTE"));

            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("EntityManager chiuso: errore interno con la causa")
        void entityManagerChiuso() throws Exception {
            PosizioneDAO daoGuasto = daoConEntityManagerChiuso();

            TributiException ex = assertThrows(TributiException.class,
                    () -> daoGuasto.aggiornaPosizioneInCorso("IUV-1"));

            assertTrue(ex.getCause() instanceof IllegalStateException);
        }
    }

    // ------------------------------------------------------------------
    // aggiornaPosizionePagata
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("aggiornaPosizionePagata")
    class Pagata {

        @Test
        @DisplayName("imposta stato PAGATA e data di pagamento, scritti sul DB anche senza merge")
        void impostaPagata() throws Exception {
            salva(ente, posizione("IUV-1", "IN_CORSO"));
            Date ieri = new Date(System.currentTimeMillis() - GIORNO);

            dao.aggiornaPosizionePagata("IUV-1", ieri);
            em.flush();
            em.clear();

            Posizione p = rileggi("IUV-1");
            assertEquals("PAGATA", p.getStato());
            assertEquals(giorno(ieri), giorno(p.getDataPagamento()));
        }

        @Test
        @DisplayName("IUV inesistente: TributiException del DAO, senza causa tecnica")
        void iuvInesistente() {
            TributiException ex = assertThrows(TributiException.class,
                    () -> dao.aggiornaPosizionePagata("NON_ESISTE", new Date()));

            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("EntityManager chiuso: errore interno con la causa")
        void entityManagerChiuso() throws Exception {
            PosizioneDAO daoGuasto = daoConEntityManagerChiuso();

            TributiException ex = assertThrows(TributiException.class,
                    () -> daoGuasto.aggiornaPosizionePagata("IUV-1", new Date()));

            assertTrue(ex.getCause() instanceof IllegalStateException);
        }
    }

    // ------------------------------------------------------------------
    // findPosizioneByIUV
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("findPosizioneByIUV")
    class PerIuv {

        @Test
        @DisplayName("restituisce il DTO con tutti i campi, ente compreso")
        void mappaturaCompleta() throws Exception {
            Posizione p = posizione("IUV-1", "PAGATA");
            p.setImporto(new BigDecimal("130.00"));
            p.setImportoOriginale(new BigDecimal("100.00"));
            Date pagamento = new Date(System.currentTimeMillis() - GIORNO);
            p.setDataPagamento(pagamento);
            salva(ente, p);

            PosizioneDTO dto = dao.findPosizioneByIUV("IUV-1");

            assertEquals("IUV-1", dto.getIuv());
            assertEquals(CF, dto.getCodiceFiscale());
            assertEquals("TARI", dto.getTipoTributo());
            assertEquals(0, new BigDecimal("130.00").compareTo(dto.getImporto()));
            assertEquals(0, new BigDecimal("100.00").compareTo(dto.getImportoOriginale()));
            assertEquals("PAGATA", dto.getStato());
            assertEquals(giorno(pagamento), giorno(dto.getDataPagamento()));
            assertEquals("C_F839", dto.getCodiceEnte());
            assertEquals("Comune di Napoli", dto.getEnteEmittente());
            assertEquals("tributi@comune.napoli.it", dto.getContattoEnte());
        }

        @Test
        @DisplayName("IUV inesistente: TributiException del DAO, senza causa tecnica")
        void iuvInesistente() {
            TributiException ex = assertThrows(TributiException.class,
                    () -> dao.findPosizioneByIUV("NON_ESISTE"));

            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("posizione senza ente: NullPointerException convertita in errore interno")
        void senzaEnte() {
            Posizione orfana = posizione("ORFANA", "DA_PAGARE");
            orfana.setEnte(null);
            salva(orfana);

            TributiException ex = assertThrows(TributiException.class,
                    () -> dao.findPosizioneByIUV("ORFANA"));

            assertTrue(ex.getCause() instanceof NullPointerException);
        }
    }

    // ------------------------------------------------------------------
    // findPosizioniByCodiceFiscale
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("findPosizioniByCodiceFiscale")
    class PerCodiceFiscale {

        @Test
        @DisplayName("restituisce solo le posizioni di quel codice fiscale, in qualsiasi stato")
        void filtraPerCodiceFiscale() {
            Posizione altroCittadino = posizione("ALTRO", "DA_PAGARE");
            altroCittadino.setCodiceFiscale("VRDLGI75B02H501Y");
            salva(ente,
                    posizione("IUV-1", "DA_PAGARE"),
                    posizione("IUV-2", "PAGATA"),
                    altroCittadino);

            Set<String> iuv = new HashSet<>();
            for (PosizioneDTO dto : dao.findPosizioniByCodiceFiscale(CF)) {
                iuv.add(dto.getIuv());
            }

            assertEquals(2, iuv.size());
            assertTrue(iuv.contains("IUV-1"));
            assertTrue(iuv.contains("IUV-2"));
        }

        @Test
        @DisplayName("codice fiscale senza posizioni: lista vuota")
        void nessunaPosizione() {
            salva(ente, posizione("IUV-1", "DA_PAGARE"));

            assertTrue(dao.findPosizioniByCodiceFiscale("VRDLGI75B02H501Y").isEmpty());
        }

        @Test
        @DisplayName("posizione senza ente: NullPointerException NON gestita")
        void senzaEnte() {
            Posizione orfana = posizione("ORFANA", "DA_PAGARE");
            orfana.setEnte(null);
            salva(orfana);

            assertThrows(NullPointerException.class, () -> dao.findPosizioniByCodiceFiscale(CF));
        }
    }

    // ------------------------------------------------------------------
    // createPagamento
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("createPagamento")
    class Creazione {

        @Test
        @DisplayName("salva la posizione con data di pagamento vuota e la restituisce")
        void creaPosizione() throws Exception {
            em.persist(ente);
            em.flush();
            Date oggi = new Date();
            Date scadenza = new Date(System.currentTimeMillis() + 30 * GIORNO);

            Posizione creata = dao.createPagamento("IUV-NEW", CF, "MULTA",
                    new BigDecimal("80.00"), new BigDecimal("80.00"),
                    "DA_PAGARE", oggi, scadenza, ente);

            assertEquals("IUV-NEW", creata.getIuv());
            assertNull(creata.getDataPagamento());

            em.clear();
            PosizioneDTO dto = dao.findPosizioneByIUV("IUV-NEW");
            assertEquals("MULTA", dto.getTipoTributo());
            assertEquals("DA_PAGARE", dto.getStato());
            assertEquals(0, new BigDecimal("80.00").compareTo(dto.getImporto()));
            assertEquals(giorno(scadenza), giorno(dto.getDataScadenza()));
            assertNull(dto.getDataPagamento());
            assertEquals("C_F839", dto.getCodiceEnte());
        }

        @Test
        @DisplayName("senza transazione attiva: TransactionRequiredException NON gestita")
        void senzaTransazione() {
            em.getTransaction().rollback();

            assertThrows(TransactionRequiredException.class,
                    () -> dao.createPagamento("IUV-NEW", CF, "MULTA",
                            new BigDecimal("80.00"), new BigDecimal("80.00"),
                            "DA_PAGARE", new Date(), new Date(), null));
        }
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private void salva(Object... entita) {
        for (Object e : entita) {
            em.persist(e);
        }
        em.flush();
        em.clear(); // il DAO rilegge dal DB, non dalla cache
    }

    private Posizione posizione(String iuv, String stato) {
        Posizione p = new Posizione();
        p.setIuv(iuv);
        p.setCodiceFiscale(CF);
        p.setTipoTributo("TARI");
        p.setStato(stato);
        p.setImporto(new BigDecimal("100.00"));
        p.setImportoOriginale(new BigDecimal("100.00"));
        p.setDataEmissione(new Date());
        p.setDataScadenza(new Date(System.currentTimeMillis() + 30 * GIORNO));
        p.setEnte(ente);
        return p;
    }

    private Posizione rileggi(String iuv) {
        List<Posizione> risultato = em
                .createQuery("SELECT p FROM Posizione p WHERE p.iuv = :iuv", Posizione.class)
                .setParameter("iuv", iuv)
                .getResultList();
        assertEquals(1, risultato.size());
        return risultato.get(0);
    }

    private PosizioneDAO daoConEntityManagerChiuso() throws Exception {
        EntityManager chiuso = emf.createEntityManager();
        chiuso.close();
        PosizioneDAO d = new PosizioneDAO();
        inietta(d, chiuso);
        return d;
    }

    private static String giorno(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd").format(data);
    }

    private static void inietta(PosizioneDAO dao, EntityManager em) throws Exception {
        Field f = PosizioneDAO.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(dao, em);
    }
}