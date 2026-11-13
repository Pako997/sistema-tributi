package it.pa.tributi.context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Hashtable;

import javax.management.ServiceNotFoundException;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.spi.InitialContextFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServiceLocatorTest {

    public static class FactoryDiTest implements InitialContextFactory {
        static Context contesto;

        @Override
        public Context getInitialContext(Hashtable<?, ?> env) {
            return contesto;
        }
    }

    interface IServizioFinto { }
    interface ServizioSenzaPrefisso { }

    private String factoryOriginale;

    @BeforeEach
    void installaContestoFinto() {
        factoryOriginale = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, FactoryDiTest.class.getName());
        FactoryDiTest.contesto = mock(Context.class);
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
    @DisplayName("il nome JNDI è java:module/ + nome dell'interfaccia senza la 'I' iniziale")
    void risolveIlBeanTogliendoLaI() throws Exception {
        IServizioFinto impl = mock(IServizioFinto.class);
        when(FactoryDiTest.contesto.lookup("java:module/ServizioFinto")).thenReturn(impl);

        IServizioFinto risultato = ServiceLocator.get(IServizioFinto.class);

        assertSame(impl, risultato);
    }

    @Test
    @DisplayName("CARATTERIZZAZIONE: se l'interfaccia non inizia con 'I' viene tolta comunque la prima lettera")
    void interfacciaSenzaPrefissoPerdeLaPrimaLettera() throws Exception {
        ServizioSenzaPrefisso impl = mock(ServizioSenzaPrefisso.class);
        when(FactoryDiTest.contesto.lookup("java:module/ervizioSenzaPrefisso")).thenReturn(impl);

        assertSame(impl, ServiceLocator.get(ServizioSenzaPrefisso.class));
    }

    @Test
    @DisplayName("bean non trovato: NamingException convertita, ma la causa originale va persa")
    void beanNonTrovato() throws Exception {
        when(FactoryDiTest.contesto.lookup("java:module/ServizioFinto"))
                .thenThrow(new NameNotFoundException("nessun binding"));

        ServiceNotFoundException ex = assertThrows(ServiceNotFoundException.class,
                () -> ServiceLocator.get(IServizioFinto.class));

        assertEquals("Non trovato: " + IServizioFinto.class.getName(), ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("CARATTERIZZAZIONE: oggetto di tipo sbagliato -> ClassCastException non gestita")
    void oggettoDiTipoSbagliato() throws Exception {
        when(FactoryDiTest.contesto.lookup("java:module/ServizioFinto")).thenReturn("non sono un servizio");

        assertThrows(ClassCastException.class, () -> ServiceLocator.get(IServizioFinto.class));
    }
}