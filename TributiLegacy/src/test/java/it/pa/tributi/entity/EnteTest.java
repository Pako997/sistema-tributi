package it.pa.tributi.entity;

import static org.junit.jupiter.api.Assertions.*;

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

@DisplayName("Ente - mapping JPA")
class EnteTest {

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
    @DisplayName("un Ente nuovo ha tutti i campi null")
    void campiInizialiNull() {
        Ente e = new Ente();

        assertNull(e.getCodiceEnte());
        assertNull(e.getDenominazione());
        assertNull(e.getContatto());
    }

    @Test
    @DisplayName("ogni setter valorizza il proprio campo")
    void getterSetter() {
        Ente e = ente("C_F839", "Comune di Napoli", "tributi@comune.napoli.it");

        assertEquals("C_F839", e.getCodiceEnte());
        assertEquals("Comune di Napoli", e.getDenominazione());
        assertEquals("tributi@comune.napoli.it", e.getContatto());
    }


    @Test
    @DisplayName("rilettura per codice: tutti i campi vengono conservati")
    void rilettura() {
        salva(ente("C_F839", "Comune di Napoli", "tributi@comune.napoli.it"));
        em.clear();

        Ente letto = em.find(Ente.class, "C_F839");

        assertEquals("C_F839", letto.getCodiceEnte());
        assertEquals("Comune di Napoli", letto.getDenominazione());
        assertEquals("tributi@comune.napoli.it", letto.getContatto());
    }

    @Test
    @DisplayName("contatto null: accettato")
    void contattoNull() {
        salva(ente("C_F839", "Comune di Napoli", null));
        em.clear();

        assertNull(em.find(Ente.class, "C_F839").getContatto());
    }

    @Test
    @DisplayName("codice inesistente: find restituisce null")
    void codiceInesistente() {
        assertNull(em.find(Ente.class, "NON_ESISTE"));
    }


    @Test
    @DisplayName("codice ente duplicato: rifiutato")
    void codiceDuplicato() {
        salva(ente("C_F839", "Comune di Napoli", null));
        em.clear();

        assertThrows(PersistenceException.class,
                () -> salva(ente("C_F839", "Altro Comune", null)));
    }

    @Test
    @DisplayName("codice ente null: rifiutato (la chiave va assegnata a mano)")
    void codiceNull() {
        assertThrows(PersistenceException.class,
                () -> salva(ente(null, "Comune di Napoli", null)));
    }

    @Test
    @DisplayName("denominazione null: rifiutata")
    void denominazioneNull() {
        assertThrows(PersistenceException.class,
                () -> salva(ente("C_F839", null, null)));
    }

    @Test
    @DisplayName("codice ente oltre 50 caratteri: rifiutato")
    void codiceTroppoLungo() {
        assertThrows(PersistenceException.class,
                () -> salva(ente(ripeti('X', 51), "Comune di Napoli", null)));
    }

    @Test
    @DisplayName("contatto oltre 100 caratteri: rifiutato")
    void contattoTroppoLungo() {
        assertThrows(PersistenceException.class,
                () -> salva(ente("C_F839", "Comune di Napoli", ripeti('x', 101))));
    }

    @Test
    @DisplayName("lunghezze massime esatte (50 e 100): accettate")
    void lunghezzeMassime() {
        salva(ente(ripeti('X', 50), "Comune di Napoli", ripeti('x', 100)));
        em.clear();

        assertNotNull(em.find(Ente.class, ripeti('X', 50)));
    }


    private void salva(Ente e) {
        em.persist(e);
        em.flush();
    }

    private static Ente ente(String codice, String denominazione, String contatto) {
        Ente e = new Ente();
        e.setCodiceEnte(codice);
        e.setDenominazione(denominazione);
        e.setContatto(contatto);
        return e;
    }

    private static String ripeti(char c, int volte) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < volte; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}