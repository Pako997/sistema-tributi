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
import javax.persistence.criteria.CriteriaQuery;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.pa.tributi.entity.ArchivioPagamento;
import it.pa.tributi.exception.TributiException;

@DisplayName("PagamentoDAO - test d'integrazione")
class PagamentoDAOTest {

    private static final long GIORNO = 24L * 60 * 60 * 1000;

    private static EntityManagerFactory emf;

    private EntityManager em;
    private PagamentoDAO dao;

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
        dao = new PagamentoDAO();
        inietta(dao, em);
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

    @Test
    @DisplayName("registraPagamento: salva iuv, id transazione e data di pagamento")
    void registraPagamento() throws Exception {
        Date ieri = new Date(System.currentTimeMillis() - GIORNO);

        dao.registraPagamento("IUV-1", "TRX-001", ieri);
        em.clear(); // rilegge dal DB

        List<ArchivioPagamento> archivio = leggiArchivio();
        assertEquals(1, archivio.size());
        ArchivioPagamento a = archivio.get(0);
        assertEquals("IUV-1", a.getIuv());
        assertEquals("TRX-001", a.getIdTransazione());
        assertEquals(giorno(ieri), giorno(a.getDataPagamento()));
    }

    @Test
    @DisplayName("registraPagamento: pagamenti diversi vengono salvati tutti")
    void piuPagamenti() throws Exception {
        dao.registraPagamento("IUV-1", "TRX-001", new Date());
        dao.registraPagamento("IUV-2", "TRX-002", new Date());
        em.clear();

        Set<String> iuv = new HashSet<>();
        for (ArchivioPagamento a : leggiArchivio()) {
            iuv.add(a.getIuv());
        }

        assertEquals(2, iuv.size());
        assertTrue(iuv.contains("IUV-1"));
        assertTrue(iuv.contains("IUV-2"));
    }

    @Test
    @DisplayName("senza transazione attiva: il flush fallisce, errore interno con la causa")
    void senzaTransazione() {
        em.getTransaction().rollback();

        TributiException ex = assertThrows(TributiException.class,
                () -> dao.registraPagamento("IUV-1", "TRX-001", new Date()));

        assertTrue(ex.getCause() instanceof TransactionRequiredException);
    }

    @Test
    @DisplayName("EntityManager chiuso: errore interno con la causa")
    void entityManagerChiuso() throws Exception {
        EntityManager chiuso = emf.createEntityManager();
        chiuso.close();
        PagamentoDAO daoGuasto = new PagamentoDAO();
        inietta(daoGuasto, chiuso);

        TributiException ex = assertThrows(TributiException.class,
                () -> daoGuasto.registraPagamento("IUV-1", "TRX-001", new Date()));

        assertTrue(ex.getCause() instanceof IllegalStateException);
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    /** Legge tutta la tabella senza dipendere dal nome JPQL dell'entità. */
    private List<ArchivioPagamento> leggiArchivio() {
        CriteriaQuery<ArchivioPagamento> q = em.getCriteriaBuilder().createQuery(ArchivioPagamento.class);
        q.select(q.from(ArchivioPagamento.class));
        return em.createQuery(q).getResultList();
    }

    private static String giorno(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd").format(data);
    }

    private static void inietta(PagamentoDAO dao, EntityManager em) throws Exception {
        Field f = PagamentoDAO.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(dao, em);
    }
}