package it.pa.tributi.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
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
import org.junit.jupiter.api.Test;

import it.pa.tributi.entity.Notifica;
import it.pa.tributi.exception.TributiException;

class NotificaDAOTest {

    private static EntityManagerFactory emf;

    private EntityManager em;
    private NotificaDAO dao;

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
        dao = new NotificaDAO();
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
    @DisplayName("registraNotifica: salva iuv, tipo e data di invio di oggi")
    void registraNotifica() throws Exception {
        dao.registraNotifica("IUV-1", "EMISSIONE");
        em.clear(); // rilegge dal DB

        List<Notifica> notifiche = dao.visualNotifica();

        assertEquals(1, notifiche.size());
        Notifica n = notifiche.get(0);
        assertEquals("IUV-1", n.getIuv());
        assertEquals("EMISSIONE", n.getTipo());
        assertNotNull(n.getId());
        assertEquals(giorno(new Date()), giorno(n.getDataInvio()));
    }

    @Test
    @DisplayName("registraNotifica: più tipi per lo stesso IUV vengono salvati tutti")
    void piuTipiStessoIuv() throws Exception {
        dao.registraNotifica("IUV-1", "EMISSIONE");
        dao.registraNotifica("IUV-1", "SOLLECITO30");
        dao.registraNotifica("IUV-1", "SOLLECITO15");
        em.clear();

        Set<String> tipi = new HashSet<>();
        for (Notifica n : dao.visualNotifica()) {
            tipi.add(n.getTipo());
        }

        assertEquals(3, tipi.size());
        assertTrue(tipi.contains("EMISSIONE"));
        assertTrue(tipi.contains("SOLLECITO30"));
        assertTrue(tipi.contains("SOLLECITO15"));
    }

    @Test
    @DisplayName("registraNotifica senza transazione attiva: il flush fallisce, errore interno con la causa")
    void senzaTransazione() {
        em.getTransaction().rollback();

        TributiException ex = assertThrows(TributiException.class,
                () -> dao.registraNotifica("IUV-1", "EMISSIONE"));

        assertTrue(ex.getCause() instanceof TransactionRequiredException);
    }

    @Test
    @DisplayName("registraNotifica con EntityManager chiuso: errore interno con la causa")
    void registraConEntityManagerChiuso() throws Exception {
        NotificaDAO daoGuasto = daoConEntityManagerChiuso();

        TributiException ex = assertThrows(TributiException.class,
                () -> daoGuasto.registraNotifica("IUV-1", "EMISSIONE"));

        assertTrue(ex.getCause() instanceof IllegalStateException);
    }

    @Test
    @DisplayName("visualNotifica: tabella vuota -> lista vuota")
    void visualNotificaVuota() {
        assertTrue(dao.visualNotifica().isEmpty());
    }

    @Test
    @DisplayName("visualNotifica: restituisce tutte le notifiche di tutti gli IUV")
    void visualNotificaTutte() throws Exception {
        dao.registraNotifica("IUV-1", "EMISSIONE");
        dao.registraNotifica("IUV-2", "EMISSIONE");
        em.clear();

        Set<String> iuv = new HashSet<>();
        for (Notifica n : dao.visualNotifica()) {
            iuv.add(n.getIuv());
        }

        assertEquals(2, iuv.size());
        assertTrue(iuv.contains("IUV-1"));
        assertTrue(iuv.contains("IUV-2"));
    }

    @Test
    @DisplayName("visualNotifica con EntityManager chiuso: eccezione NON convertita in TributiException")
    void visualConEntityManagerChiuso() throws Exception {
        NotificaDAO daoGuasto = daoConEntityManagerChiuso();

        assertThrows(IllegalStateException.class, daoGuasto::visualNotifica);
    }


    private NotificaDAO daoConEntityManagerChiuso() throws Exception {
        EntityManager chiuso = emf.createEntityManager();
        chiuso.close();
        NotificaDAO d = new NotificaDAO();
        inietta(d, chiuso);
        return d;
    }

    private static String giorno(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd").format(data);
    }

    private static void inietta(NotificaDAO dao, EntityManager em) throws Exception {
        Field f = NotificaDAO.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(dao, em);
    }
}