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

import it.pa.tributi.entity.Operatore;

@DisplayName("OperatoreDAO - test d'integrazione")
class OperatoreDAOTest {

    private static EntityManagerFactory emf;

    private EntityManager em;
    private OperatoreDAO dao;

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
        dao = new OperatoreDAO();
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
    @DisplayName("codice fiscale registrato come operatore -> OPERATORE")
    void operatoreRegistrato() {
        salva(operatore("VRDLGI75B02H501Y", "SEDE_NAPOLI"));

        assertEquals("OPERATORE", dao.findOperatoreByCodiceFiscale("VRDLGI75B02H501Y"));
    }

    @Test
    @DisplayName("operatore senza sede -> OPERATORE")
    void operatoreSenzaSede() {
        salva(operatore("VRDLGI75B02H501Y", null));

        assertEquals("OPERATORE", dao.findOperatoreByCodiceFiscale("VRDLGI75B02H501Y"));
    }

    @Test
    @DisplayName("codice fiscale non registrato -> CITTADINO")
    void cittadino() {
        salva(operatore("VRDLGI75B02H501Y", "SEDE_NAPOLI"));

        assertEquals("CITTADINO", dao.findOperatoreByCodiceFiscale("RSSMRA80A01F839X"));
    }

    @Test
    @DisplayName("tabella operatori vuota -> CITTADINO")
    void nessunOperatore() {
        assertEquals("CITTADINO", dao.findOperatoreByCodiceFiscale("RSSMRA80A01F839X"));
    }

    @Test
    @DisplayName("codice fiscale null -> IllegalArgumentException non gestita")
    void codiceFiscaleNull() {
        assertThrows(IllegalArgumentException.class,
                () -> dao.findOperatoreByCodiceFiscale(null));
    }

    @Test
    @DisplayName("EntityManager chiuso -> IllegalStateException non gestita")
    void entityManagerChiuso() throws Exception {
        EntityManager chiuso = emf.createEntityManager();
        chiuso.close();
        OperatoreDAO daoGuasto = new OperatoreDAO();
        inietta(daoGuasto, chiuso);

        assertThrows(IllegalStateException.class,
                () -> daoGuasto.findOperatoreByCodiceFiscale("VRDLGI75B02H501Y"));
    }


    private void salva(Object... entita) {
        for (Object e : entita) {
            em.persist(e);
        }
        em.flush();
        em.clear();
    }

    private static Operatore operatore(String codiceFiscale, String codiceSede) {
        Operatore o = new Operatore();
        o.setCodiceFiscale(codiceFiscale);
        o.setCodiceSede(codiceSede);
        return o;
    }

    private static void inietta(OperatoreDAO dao, EntityManager em) throws Exception {
        Field f = OperatoreDAO.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(dao, em);
    }
}
