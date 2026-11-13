package it.pa.tributi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.spi.InitialContextFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.pa.tributi.dao.IOperatoreDAO;

@DisplayName("OperatoreService")
class OperatoreServiceTest {

    private static final String JNDI_DAO = "java:module/OperatoreDAO";
    private static final String CF = "VRDLGI75B02H501Y";

    /** Factory JNDI di test: deve essere pubblica e avere un costruttore vuoto. */
    public static class FactoryDiTest implements InitialContextFactory {
        static Context contesto;

        @Override
        public Context getInitialContext(Hashtable<?, ?> env) {
            return contesto;
        }
    }

    private final OperatoreService service = new OperatoreService();

    private String factoryOriginale;
    private Context contesto;
    private IOperatoreDAO dao;

    @BeforeEach
    void installaContestoFinto() throws Exception {
        factoryOriginale = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, FactoryDiTest.class.getName());

        contesto = mock(Context.class);
        dao = mock(IOperatoreDAO.class);
        FactoryDiTest.contesto = contesto;
        lenient().when(contesto.lookup(JNDI_DAO)).thenReturn(dao);
    }

    @AfterEach
    void ripristina() {
        if (factoryOriginale == null) {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        } else {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, factoryOriginale);
        }
        FactoryDiTest.contesto = null;
    }

    @Test
    @DisplayName("codice fiscale di un operatore -> OPERATORE")
    void operatore() throws Exception {
        when(dao.findOperatoreByCodiceFiscale(CF)).thenReturn("OPERATORE");

        assertEquals("OPERATORE", service.getRuolo(CF));
    }

    @Test
    @DisplayName("codice fiscale di un cittadino -> CITTADINO")
    void cittadino() throws Exception {
        when(dao.findOperatoreByCodiceFiscale("RSSMRA80A01F839X")).thenReturn("CITTADINO");

        assertEquals("CITTADINO", service.getRuolo("RSSMRA80A01F839X"));
    }

    @Test
    @DisplayName("restituisce esattamente quello che risponde il DAO, senza modificarlo")
    void passaIlValoreDelDao() throws Exception {
        when(dao.findOperatoreByCodiceFiscale(CF)).thenReturn("VALORE_QUALSIASI");

        assertEquals("VALORE_QUALSIASI", service.getRuolo(CF));
    }

    @Test
    @DisplayName("passa al DAO il codice fiscale così com'è, anche null")
    void passaIlCodiceFiscale() throws Exception {
        service.getRuolo(null);

        verify(dao).findOperatoreByCodiceFiscale(null);
    }

    @Test
    @DisplayName("cerca il DAO con il nome JNDI java:module/OperatoreDAO")
    void nomeJndi() throws Exception {
        service.getRuolo(CF);

        verify(contesto).lookup(JNDI_DAO);
    }

    @Test
    @DisplayName("ogni chiamata ripete la ricerca JNDI del DAO")
    void lookupAdOgniChiamata() throws Exception {
        service.getRuolo(CF);
        service.getRuolo(CF);

        verify(contesto, times(2)).lookup(JNDI_DAO);
    }

    @Test
    @DisplayName("DAO non registrato: ServiceNotFoundException, il DAO non viene chiamato")
    void daoNonTrovato() throws Exception {
        when(contesto.lookup(JNDI_DAO)).thenThrow(new NameNotFoundException("nessun binding"));

        Exception ex = assertThrows(Exception.class, () -> service.getRuolo(CF));

        assertEquals("ServiceNotFoundException", ex.getClass().getSimpleName());
        verifyNoInteractions(dao);
    }

    @Test
    @DisplayName("errore del DAO: l'eccezione esce così com'è")
    void erroreDelDao() throws Exception {
        IllegalStateException errore = new IllegalStateException("EntityManager chiuso");
        when(dao.findOperatoreByCodiceFiscale(CF)).thenThrow(errore);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.getRuolo(CF));

        assertSame(errore, ex);
    }
}