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

@DisplayName("ArchivioPagamento - mapping JPA")
class ArchivioPagamentoTest {

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


    @Test
    @DisplayName("prima del salvataggio l'id è null, gli altri campi sono quelli impostati")
    void getterSetter() {
        Date data = new Date(1701000000000L);
        ArchivioPagamento a = pagamento("IUV-1", "TRX-001", data);

        assertNull(a.getId());
        assertEquals("IUV-1", a.getIuv());
        assertEquals("TRX-001", a.getIdTransazione());
        assertEquals(data, a.getDataPagamento());
    }


    @Test
    @DisplayName("salvataggio: l'id viene generato dal database")
    void idGenerato() {
        ArchivioPagamento a = pagamento("IUV-1", "TRX-001", new Date());

        em.persist(a);
        em.flush();

        assertNotNull(a.getId());
    }

    @Test
    @DisplayName("rilettura: tutti i campi vengono conservati")
    void rilettura() {
        Date data = new Date(1701000000000L);
        ArchivioPagamento a = pagamento("IUV-1", "TRX-001", data);
        em.persist(a);
        em.flush();
        em.clear();

        ArchivioPagamento letto = em.find(ArchivioPagamento.class, a.getId());

        assertEquals("IUV-1", letto.getIuv());
        assertEquals("TRX-001", letto.getIdTransazione());
        assertEquals(giorno(data), giorno(letto.getDataPagamento()));
    }

    @Test
    @DisplayName("data_pagamento è DATE: l'ora viene persa, si rilegge la mezzanotte")
    void oraPersa() {
        Calendar pomeriggio = Calendar.getInstance();
        pomeriggio.set(2026, Calendar.MARCH, 10, 15, 45, 30);
        ArchivioPagamento a = pagamento("IUV-1", "TRX-001", pomeriggio.getTime());
        em.persist(a);
        em.flush();
        em.clear();

        Calendar letta = Calendar.getInstance();
        letta.setTime(em.find(ArchivioPagamento.class, a.getId()).getDataPagamento());

        assertEquals("2026-03-10", giorno(letta.getTime()));
        assertEquals(0, letta.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, letta.get(Calendar.MINUTE));
        assertEquals(0, letta.get(Calendar.SECOND));
    }

    @Test
    @DisplayName("iuv duplicato: rifiutato")
    void iuvDuplicato() {
        salva(pagamento("IUV-1", "TRX-001", new Date()));

        assertThrows(PersistenceException.class,
                () -> salva(pagamento("IUV-1", "TRX-002", new Date())));
    }

    @Test
    @DisplayName("id transazione duplicato: rifiutato")
    void idTransazioneDuplicato() {
        salva(pagamento("IUV-1", "TRX-001", new Date()));

        assertThrows(PersistenceException.class,
                () -> salva(pagamento("IUV-2", "TRX-001", new Date())));
    }

    @Test
    @DisplayName("iuv null: rifiutato")
    void iuvNull() {
        assertThrows(PersistenceException.class,
                () -> salva(pagamento(null, "TRX-001", new Date())));
    }

    @Test
    @DisplayName("id transazione null: rifiutato")
    void idTransazioneNull() {
        assertThrows(PersistenceException.class,
                () -> salva(pagamento("IUV-1", null, new Date())));
    }

    @Test
    @DisplayName("data pagamento null: rifiutata")
    void dataNull() {
        assertThrows(PersistenceException.class,
                () -> salva(pagamento("IUV-1", "TRX-001", null)));
    }

    @Test
    @DisplayName("pagamenti con iuv e id transazione diversi: accettati entrambi")
    void pagamentiDistinti() {
        salva(pagamento("IUV-1", "TRX-001", new Date()));
        salva(pagamento("IUV-2", "TRX-002", new Date()));

        Long totale = em.createQuery("SELECT COUNT(a) FROM ArchivioPagamento a", Long.class)
                .getSingleResult();
        assertEquals(2L, totale.longValue());
    }


    private void salva(ArchivioPagamento a) {
        em.persist(a);
        em.flush();
    }

    private static ArchivioPagamento pagamento(String iuv, String idTransazione, Date data) {
        ArchivioPagamento a = new ArchivioPagamento();
        a.setIuv(iuv);
        a.setIdTransazione(idTransazione);
        a.setDataPagamento(data);
        return a;
    }

    private static String giorno(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd").format(data);
    }
}