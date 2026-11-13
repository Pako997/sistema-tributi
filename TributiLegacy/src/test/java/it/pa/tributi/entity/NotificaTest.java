package it.pa.tributi.entity;

import static org.junit.jupiter.api.Assertions.*;

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
import org.junit.jupiter.api.Test;

@DisplayName("Notifica - mapping JPA")
class NotificaTest {

    private static EntityManagerFactory emf;

    private EntityManager em;

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
    // Getter e setter
    // ------------------------------------------------------------------

    @Test
    @DisplayName("una Notifica nuova ha tutti i campi null")
    void campiInizialiNull() {
        Notifica n = new Notifica();

        assertNull(n.getId());
        assertNull(n.getIuv());
        assertNull(n.getDataInvio());
        assertNull(n.getTipo());
    }

    @Test
    @DisplayName("ogni setter valorizza il proprio campo")
    void getterSetter() {
        Date data = new Date(1701000000000L);
        Notifica n = notifica("IUV-1", "EMISSIONE", data);
        n.setId(5L);

        assertEquals(Long.valueOf(5L), n.getId());
        assertEquals("IUV-1", n.getIuv());
        assertEquals("EMISSIONE", n.getTipo());
        assertEquals(data, n.getDataInvio());
    }

    // ------------------------------------------------------------------
    // Salvataggio e rilettura
    // ------------------------------------------------------------------

    @Test
    @DisplayName("salvataggio: l'id viene generato dal database")
    void idGenerato() {
        Notifica n = notifica("IUV-1", "EMISSIONE", new Date());

        salva(n);

        assertNotNull(n.getId());
    }

    @Test
    @DisplayName("rilettura: tutti i campi vengono conservati")
    void rilettura() {
        Date data = new Date(1701000000000L);
        Notifica n = notifica("IUV-1", "SOLLECITO15", data);
        salva(n);
        em.clear();

        Notifica letta = em.find(Notifica.class, n.getId());

        assertEquals("IUV-1", letta.getIuv());
        assertEquals("SOLLECITO15", letta.getTipo());
        assertEquals(giorno(data), giorno(letta.getDataInvio()));
    }

    @Test
    @DisplayName("data_invio è DATE: l'ora viene persa, si rilegge la mezzanotte")
    void oraPersa() {
        Calendar pomeriggio = Calendar.getInstance();
        pomeriggio.set(2026, Calendar.MARCH, 10, 15, 45, 30);
        Notifica n = notifica("IUV-1", "EMISSIONE", pomeriggio.getTime());
        salva(n);
        em.clear();

        Calendar letta = Calendar.getInstance();
        letta.setTime(em.find(Notifica.class, n.getId()).getDataInvio());

        assertEquals("2026-03-10", giorno(letta.getTime()));
        assertEquals(0, letta.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, letta.get(Calendar.MINUTE));
        assertEquals(0, letta.get(Calendar.SECOND));
    }

    // ------------------------------------------------------------------
    // Vincoli
    // ------------------------------------------------------------------

    @Test
    @DisplayName("iuv null: rifiutato")
    void iuvNull() {
        assertThrows(PersistenceException.class,
                () -> salva(notifica(null, "EMISSIONE", new Date())));
    }

    @Test
    @DisplayName("tipo null: rifiutato")
    void tipoNull() {
        assertThrows(PersistenceException.class,
                () -> salva(notifica("IUV-1", null, new Date())));
    }

    @Test
    @DisplayName("data invio null: rifiutata")
    void dataNull() {
        assertThrows(PersistenceException.class,
                () -> salva(notifica("IUV-1", "EMISSIONE", null)));
    }

    @Test
    @DisplayName("iuv oltre 35 caratteri: rifiutato")
    void iuvTroppoLungo() {
        assertThrows(PersistenceException.class,
                () -> salva(notifica(ripeti('X', 36), "EMISSIONE", new Date())));
    }

    @Test
    @DisplayName("tipo oltre 20 caratteri: rifiutato")
    void tipoTroppoLungo() {
        assertThrows(PersistenceException.class,
                () -> salva(notifica("IUV-1", ripeti('X', 21), new Date())));
    }

    @Test
    @DisplayName("lunghezze massime esatte (35 e 20): accettate")
    void lunghezzeMassime() {
        Notifica n = notifica(ripeti('X', 35), ripeti('Y', 20), new Date());

        salva(n);

        assertNotNull(n.getId());
    }

    @Test
    @DisplayName("stessa coppia iuv + tipo due volte: accettata (l'entità non dichiara il vincolo UNIQUE)")
    void duplicatoAccettato() {
        salva(notifica("IUV-1", "EMISSIONE", new Date()));
        salva(notifica("IUV-1", "EMISSIONE", new Date()));

        Long totale = em.createQuery(
                        "SELECT COUNT(n) FROM Notifica n WHERE n.iuv = 'IUV-1' AND n.tipo = 'EMISSIONE'", Long.class)
                .getSingleResult();
        assertEquals(2L, totale.longValue());
    }

    @Test
    @DisplayName("iuv senza posizione corrispondente: accettato (l'entità non dichiara la chiave esterna)")
    void senzaPosizione() {
        Notifica n = notifica("IUV-INESISTENTE", "EMISSIONE", new Date());

        salva(n);

        assertNotNull(n.getId());
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private void salva(Notifica n) {
        em.persist(n);
        em.flush();
    }

    private static Notifica notifica(String iuv, String tipo, Date data) {
        Notifica n = new Notifica();
        n.setIuv(iuv);
        n.setTipo(tipo);
        n.setDataInvio(data);
        return n;
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