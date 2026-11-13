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

@DisplayName("Operatore - mapping JPA")
class OperatoreTest {

    private static final String CF = "VRDLGI75B02H501Y";

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
            em.getTransaction().rollback();
        }
        em.close();
    }


    @Test
    @DisplayName("un Operatore nuovo ha tutti i campi null")
    void campiInizialiNull() {
        Operatore o = new Operatore();

        assertNull(o.getCodiceFiscale());
        assertNull(o.getCodiceSede());
    }

    @Test
    @DisplayName("ogni setter valorizza il proprio campo")
    void getterSetter() {
        Operatore o = operatore(CF, "SEDE_NAPOLI");

        assertEquals(CF, o.getCodiceFiscale());
        assertEquals("SEDE_NAPOLI", o.getCodiceSede());
    }


    @Test
    @DisplayName("rilettura per codice fiscale: tutti i campi vengono conservati")
    void rilettura() {
        salva(operatore(CF, "SEDE_NAPOLI"));
        em.clear();

        Operatore letto = em.find(Operatore.class, CF);

        assertEquals(CF, letto.getCodiceFiscale());
        assertEquals("SEDE_NAPOLI", letto.getCodiceSede());
    }

    @Test
    @DisplayName("sede null: accettata")
    void sedeNull() {
        salva(operatore(CF, null));
        em.clear();

        assertNull(em.find(Operatore.class, CF).getCodiceSede());
    }

    @Test
    @DisplayName("codice fiscale inesistente: find restituisce null")
    void codiceInesistente() {
        assertNull(em.find(Operatore.class, "RSSMRA80A01F839X"));
    }


    @Test
    @DisplayName("codice fiscale duplicato: rifiutato")
    void codiceDuplicato() {
        salva(operatore(CF, "SEDE_NAPOLI"));
        em.clear();

        assertThrows(PersistenceException.class,
                () -> salva(operatore(CF, "SEDE_ROMA")));
    }

    @Test
    @DisplayName("codice fiscale null: rifiutato (la chiave va assegnata a mano)")
    void codiceNull() {
        assertThrows(PersistenceException.class,
                () -> salva(operatore(null, "SEDE_NAPOLI")));
    }

    @Test
    @DisplayName("codice fiscale oltre 16 caratteri: rifiutato")
    void codiceTroppoLungo() {
        assertThrows(PersistenceException.class,
                () -> salva(operatore(CF + "X", "SEDE_NAPOLI")));
    }

    @Test
    @DisplayName("sede oltre 50 caratteri: rifiutata")
    void sedeTroppoLunga() {
        assertThrows(PersistenceException.class,
                () -> salva(operatore(CF, ripeti('S', 51))));
    }

    @Test
    @DisplayName("lunghezze massime esatte (16 e 50): accettate")
    void lunghezzeMassime() {
        salva(operatore(CF, ripeti('S', 50)));
        em.clear();

        assertNotNull(em.find(Operatore.class, CF));
    }


    private void salva(Operatore o) {
        em.persist(o);
        em.flush();
    }

    private static Operatore operatore(String codiceFiscale, String codiceSede) {
        Operatore o = new Operatore();
        o.setCodiceFiscale(codiceFiscale);
        o.setCodiceSede(codiceSede);
        return o;
    }

    private static String ripeti(char c, int volte) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < volte; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}