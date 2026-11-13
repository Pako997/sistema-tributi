package it.pa.tributi.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.pa.tributi.entity.Ente;
import it.pa.tributi.exception.TributiException;

class EnteDAOTest {

    private static EntityManagerFactory emf;

    private EntityManager em;
    private EnteDAO dao;

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
        dao = new EnteDAO();
        inietta(dao, em);
        em.getTransaction().begin();
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
    @DisplayName("ente esistente: viene restituito con tutti i campi")
    void enteEsistente() throws Exception {
        salva(ente("C_F839", "Comune di Napoli", "tributi@comune.napoli.it"));

        Ente trovato = dao.getEnteByCodice("C_F839");

        assertEquals("C_F839", trovato.getCodiceEnte());
        assertEquals("Comune di Napoli", trovato.getDenominazione());
        assertEquals("tributi@comune.napoli.it", trovato.getContatto());
    }

    @Test
    @DisplayName("ente con contatto vuoto: viene restituito, contatto null")
    void enteSenzaContatto() throws Exception {
        salva(ente("C_A001", "Comune di Prova", null));

        Ente trovato = dao.getEnteByCodice("C_A001");

        assertEquals("Comune di Prova", trovato.getDenominazione());
        assertNull(trovato.getContatto());
    }

    @Test
    @DisplayName("tra più enti restituisce quello richiesto")
    void scegliEnteGiusto() throws Exception {
        salva(ente("C_F839", "Comune di Napoli", null),
                ente("C_L259", "Comune di Torre del Greco", null));

        assertEquals("Comune di Torre del Greco", dao.getEnteByCodice("C_L259").getDenominazione());
    }

    @Test
    @DisplayName("ente inesistente: TributiException del DAO, senza causa tecnica")
    void enteInesistente() {
        TributiException ex = assertThrows(TributiException.class,
                () -> dao.getEnteByCodice("NON_ESISTE"));

        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("codice null: em.find lancia IllegalArgumentException, convertita in errore interno")
    void codiceNull() {
        TributiException ex = assertThrows(TributiException.class,
                () -> dao.getEnteByCodice(null));

        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("errore tecnico (EntityManager chiuso): convertito in errore interno con la causa")
    void erroreTecnico() throws Exception {
        EntityManager chiuso = emf.createEntityManager();
        chiuso.close();
        EnteDAO daoGuasto = new EnteDAO();
        inietta(daoGuasto, chiuso);

        TributiException ex = assertThrows(TributiException.class,
                () -> daoGuasto.getEnteByCodice("C_F839"));

        assertTrue(ex.getCause() instanceof IllegalStateException);
    }


    private void salva(Object... entita) {
        for (Object e : entita) {
            em.persist(e);
        }
        em.flush();
        em.clear();
    }

    private static Ente ente(String codice, String denominazione, String contatto) {
        Ente e = new Ente();
        e.setCodiceEnte(codice);
        e.setDenominazione(denominazione);
        e.setContatto(contatto);
        return e;
    }

    private static void inietta(EnteDAO dao, EntityManager em) throws Exception {
        Field f = EnteDAO.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(dao, em);
    }
}
