package it.pa.tributi.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.entity.Ente;
import it.pa.tributi.entity.Notifica;
import it.pa.tributi.entity.Posizione;
import it.pa.tributi.exception.TributiException;
import org.junit.jupiter.api.*;

@DisplayName("BatchNotificaDAO - test d'integrazione")
class BatchNotificaDAOTest {

    private static final long GIORNO = 24L * 60 * 60 * 1000;

    private static EntityManagerFactory emf;

    private EntityManager em;
    private BatchNotificaDAO dao;
    private Ente ente;

    @BeforeAll
    static void creaFactory() {
        emf = Persistence.createEntityManagerFactory("tributiTestPU");
    }

    @AfterAll
    static void chiudiFactory() {
        emf.close();
    }

    @BeforeEach
    void preparaDao() throws Exception {
        em = emf.createEntityManager();
        dao = new BatchNotificaDAO();
        inietta(dao, "em", em); // sostituisce @PersistenceContext
        em.getTransaction().begin();

        ente = new Ente();
        ente.setCodiceEnte("C_F839");
        ente.setDenominazione("Comune di Napoli");
        ente.setContatto("tributi@comune.napoli.it");
    }

    @AfterEach
    void pulisciDatabase() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Notifica").executeUpdate();
        em.createQuery("DELETE FROM Posizione").executeUpdate();
        em.createQuery("DELETE FROM Ente").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @Nested
    @DisplayName("findPosizioniDaNotificareEmissione")
    class Emissione {

        @Test
        @DisplayName("restituisce solo le DA_PAGARE senza notifica EMISSIONE")
        void filtraPerStatoENotifica() throws Exception {
            salva(ente,
                    posizione("NUOVA", "TARI", "DA_PAGARE", 30),
                    posizione("PAGATA", "TARI", "PAGATA", 30),
                    posizione("GIA_NOTIFICATA", "TARI", "DA_PAGARE", 30),
                    notifica("GIA_NOTIFICATA", "EMISSIONE"),
                    posizione("SOLO_SOLLECITO", "TARI", "DA_PAGARE", 30),
                    notifica("SOLO_SOLLECITO", "SOLLECITO15"));

            Set<String> trovate = iuv(dao.findPosizioniDaNotificareEmissione());

            assertEquals(set("NUOVA", "SOLO_SOLLECITO"), trovate);
        }

        @Test
        @DisplayName("CARATTERIZZAZIONE: include anche posizioni già scadute (nessun filtro sulla data)")
        void includeScadute() throws Exception {
            salva(ente, posizione("SCADUTA", "MULTA", "DA_PAGARE", -100));

            assertEquals(set("SCADUTA"), iuv(dao.findPosizioniDaNotificareEmissione()));
        }

        @Test
        @DisplayName("mappa i dati dell'ente ma NON l'importo originale")
        void mappaturaDto() throws Exception {
            Posizione p = posizione("IUV-1", "TARI", "DA_PAGARE", 30);
            p.setCodiceFiscale("RSSMRA80A01F839X");
            salva(ente, p);

            PosizioneDTO dto = dao.findPosizioniDaNotificareEmissione().get(0);

            assertEquals("RSSMRA80A01F839X", dto.getCodiceFiscale());
            assertEquals("TARI", dto.getTipoTributo());
            assertEquals(0, new BigDecimal("100.00").compareTo(dto.getImporto()));
            assertEquals("C_F839", dto.getCodiceEnte());
            assertEquals("Comune di Napoli", dto.getEnteEmittente());
            assertEquals("tributi@comune.napoli.it", dto.getContattoEnte());
            assertNull(dto.getImportoOriginale()); // mappato solo in findPosizioniScadute
        }
    }


    @Nested
    @DisplayName("findPosizioniDaNotificare(min, max, tipo)")
    class Solleciti {

        @Test
        @DisplayName("SOLLECITO15: scadenza nei PROSSIMI 15 giorni, non ancora sollecitata")
        void sollecito15() throws Exception {
            salva(ente,
                    posizione("FRA_10", "TARI", "DA_PAGARE", 10),
                    posizione("FRA_20", "TARI", "DA_PAGARE", 20),
                    posizione("SCADUTA", "TARI", "DA_PAGARE", -5),
                    posizione("PAGATA", "TARI", "PAGATA", 10),
                    posizione("GIA_SOLLECITATA", "TARI", "DA_PAGARE", 10),
                    notifica("GIA_SOLLECITATA", "SOLLECITO15"));

            Set<String> trovate = iuv(dao.findPosizioniDaNotificare(0, 15, "SOLLECITO15"));

            assertEquals(set("FRA_10"), trovate);
        }

        @Test
        @DisplayName("SOLLECITO30: scadenza tra 15 e 30 giorni")
        void sollecito30() throws Exception {
            salva(ente,
                    posizione("FRA_10", "TARI", "DA_PAGARE", 10),
                    posizione("FRA_20", "TARI", "DA_PAGARE", 20),
                    posizione("FRA_40", "TARI", "DA_PAGARE", 40));

            Set<String> trovate = iuv(dao.findPosizioniDaNotificare(15, 30, "SOLLECITO30"));

            assertEquals(set("FRA_20"), trovate);
        }

        @Test
        void confineQuindiciGiorni() throws Exception {
            salva(ente,
                    posizione("FRA_15", "TARI", "DA_PAGARE", 15),
                    posizione("FRA_16", "TARI", "DA_PAGARE", 16));

            assertEquals(set("FRA_15"), iuv(dao.findPosizioniDaNotificare(0, 15, "SOLLECITO15")));
            assertEquals(set("FRA_16"), iuv(dao.findPosizioniDaNotificare(15, 30, "SOLLECITO30")));
        }

        @Test
        @DisplayName("CARATTERIZZAZIONE: una posizione già scaduta non riceve nessun sollecito")
        void scadutaSenzaSolleciti() throws Exception {
            salva(ente, posizione("SCADUTA", "MULTA", "DA_PAGARE", -1));

            assertTrue(dao.findPosizioniDaNotificare(0, 15, "SOLLECITO15").isEmpty());
            assertTrue(dao.findPosizioniDaNotificare(15, 30, "SOLLECITO30").isEmpty());
        }
    }

    @Test
    @DisplayName("CARATTERIZZAZIONE: l'overload (giorni, tipo), non usato dal batch, include anche le scadute")
    void overloadSenzaLimiteInferiore() throws Exception {
        salva(ente,
                posizione("SCADUTA", "TARI", "DA_PAGARE", -5),
                posizione("FRA_10", "TARI", "DA_PAGARE", 10),
                posizione("FRA_20", "TARI", "DA_PAGARE", 20));

        assertEquals(set("SCADUTA", "FRA_10"), iuv(dao.findPosizioniDaNotificare(15, "QUALSIASI")));
    }

    @Test
    @DisplayName("findPosizioniScadute: solo MULTA e TARI, DA_PAGARE, con scadenza passata")
    void posizioniScadute() throws Exception {
        Posizione multa = posizione("MULTA_SCADUTA", "MULTA", "DA_PAGARE", -10);
        multa.setImportoOriginale(new BigDecimal("80.00"));
        salva(ente,
                multa,
                posizione("TARI_SCADUTA", "TARI", "DA_PAGARE", -1),
                posizione("IMU_SCADUTA", "IMU", "DA_PAGARE", -10),
                posizione("PAGATA", "MULTA", "PAGATA", -10),
                posizione("FUTURA", "TARI", "DA_PAGARE", 5));

        List<PosizioneDTO> scadute = dao.findPosizioniScadute();

        assertEquals(set("MULTA_SCADUTA", "TARI_SCADUTA"), iuv(scadute));
        PosizioneDTO dtoMulta = scadute.stream()
                .filter(d -> d.getIuv().equals("MULTA_SCADUTA")).findFirst().get();
        assertEquals(0, new BigDecimal("80.00").compareTo(dtoMulta.getImportoOriginale()));
    }


    @Nested
    @DisplayName("aggiornaImporto")
    class AggiornaImporto {

        @Test
        @DisplayName("CARATTERIZZAZIONE: l'importo viene salvato al commit anche senza em.merge (dirty checking)")
        void salvaSenzaMerge() throws Exception {
            salva(ente, posizione("IUV-1", "MULTA", "DA_PAGARE", -70));

            dao.aggiornaImporto("IUV-1", new BigDecimal("200.00"));
            em.getTransaction().commit();

            EntityManager altro = emf.createEntityManager(); // rilegge dal DB, non dalla cache
            try {
                Posizione riletta = altro
                        .createQuery("SELECT p FROM Posizione p WHERE p.iuv = :iuv", Posizione.class)
                        .setParameter("iuv", "IUV-1").getSingleResult();
                assertEquals(0, new BigDecimal("200.00").compareTo(riletta.getImporto()));
            } finally {
                altro.close();
            }
        }

        @Test
        @DisplayName("IUV inesistente: TributiException di dominio (senza causa tecnica)")
        void iuvInesistente() {
            TributiException ex = assertThrows(TributiException.class,
                    () -> dao.aggiornaImporto("NON_ESISTE", new BigDecimal("1.00")));

            // eccezione creata dal DAO, non un errore interno incapsulato
            assertNull(ex.getCause());
            // TODO: verificare anche CODICE_IUV_PAGAMENTO_NOT_FOUND con il getter reale di TributiException
        }
    }


    @Test
    @DisplayName("CARATTERIZZAZIONE: una posizione senza ente fa fallire l'intera lettura")
    void posizioneSenzaEnte() throws Exception {
        // Valido solo se la colonna FK dell'ente è nullable; altrimenti rimuovere il test.
        Posizione orfana = posizione("ORFANA", "TARI", "DA_PAGARE", 30);
        orfana.setEnte(null);
        salva(ente, posizione("BUONA", "TARI", "DA_PAGARE", 30), orfana);

        TributiException ex = assertThrows(TributiException.class,
                () -> dao.findPosizioniDaNotificareEmissione());

        assertTrue(ex.getCause() instanceof NullPointerException);
    }

    private void salva(Object... entita) {
        for (Object e : entita) {
            em.persist(e);
        }
        em.flush();
        em.clear(); // le query successive leggono dal DB, non dal contesto di persistenza
    }

    /** giorniDaOggi > 0: scade nel futuro; < 0: già scaduta. */
    private Posizione posizione(String iuv, String tipo, String stato, long giorniDaOggi) {
        Posizione p = new Posizione();
        p.setIuv(iuv);
        p.setCodiceFiscale("RSSMRA80A01F839X");
        p.setTipoTributo(tipo);
        p.setStato(stato);
        p.setImporto(new BigDecimal("100.00"));
        p.setImportoOriginale(new BigDecimal("100.00"));
        p.setDataEmissione(new Date());
        p.setDataScadenza(new Date(System.currentTimeMillis() + giorniDaOggi * GIORNO));
        p.setEnte(ente);
        return p;
    }

    private Notifica notifica(String iuv, String tipo) {
        Notifica n = new Notifica();
        n.setIuv(iuv);
        n.setTipo(tipo);
        n.setDataInvio(new Date());
        return n;
    }

    private static Set<String> iuv(List<PosizioneDTO> dto) {
        Set<String> risultato = new HashSet<>();
        for (PosizioneDTO d : dto) {
            risultato.add(d.getIuv());
        }
        return risultato;
    }

    private static Set<String> set(String... valori) {
        Set<String> s = new HashSet<>();
        for (String v : valori) {
            s.add(v);
        }
        return s;
    }

    private static void inietta(Object target, String campo, Object valore) throws Exception {
        Field f = target.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(target, valore);
    }
}
