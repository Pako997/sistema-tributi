package it.pa.tributi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.spi.InitialContextFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import it.pa.tributi.dao.IEnteDAO;
import it.pa.tributi.dao.IPosizioneDAO;
import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.entity.Ente;
import it.pa.tributi.entity.Posizione;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;

@DisplayName("PosizioneService")
class PosizioneServiceTest {

    private static final String JNDI_POSIZIONE_DAO = "java:module/PosizioneDAO";
    private static final String JNDI_ENTE_DAO = "java:module/EnteDAO";
    private static final String CF = "RSSMRA80A01F839X";

    public static class FactoryDiTest implements InitialContextFactory {
        static Context contesto;

        @Override
        public Context getInitialContext(Hashtable<?, ?> env) {
            return contesto;
        }
    }

    private final PosizioneService service = new PosizioneService();

    private String factoryOriginale;
    private Context contesto;
    private IPosizioneDAO posizioneDAO;
    private IEnteDAO enteDAO;
    private Ente ente;

    @BeforeEach
    void installaContestoFinto() throws Exception {
        factoryOriginale = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, FactoryDiTest.class.getName());

        contesto = mock(Context.class);
        posizioneDAO = mock(IPosizioneDAO.class);
        enteDAO = mock(IEnteDAO.class);
        FactoryDiTest.contesto = contesto;

        lenient().when(contesto.lookup(JNDI_POSIZIONE_DAO)).thenReturn(posizioneDAO);
        lenient().when(contesto.lookup(JNDI_ENTE_DAO)).thenReturn(enteDAO);

        ente = new Ente();
        ente.setCodiceEnte("C_F839");
        ente.setDenominazione("Comune di Napoli");
        lenient().when(enteDAO.getEnteByCodice("C_F839")).thenReturn(ente);

        lenient().when(posizioneDAO.createPagamento(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    Posizione p = new Posizione();
                    p.setIuv(inv.getArgument(0));
                    return p;
                });
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

    @Nested
    @DisplayName("aggiornaPosizioneInCorso")
    class InCorso {

        @Test
        @DisplayName("passa lo IUV al DAO")
        void delega() throws Exception {
            service.aggiornaPosizioneInCorso("IUV-1");

            verify(posizioneDAO).aggiornaPosizioneInCorso("IUV-1");
        }

        @Test
        @DisplayName("errore del DAO: esce invariato")
        void erroreDao() throws Exception {
            TributiException errore = new TributiException(
                    CodiciErrore.CODICE_IUV_PAGAMENTO_NOT_FOUND, CodiciErrore.MESSAGGIO_IUV_PAGAMENTO_NOT_FOUND);
            doThrow(errore).when(posizioneDAO).aggiornaPosizioneInCorso("NON_ESISTE");

            TributiException ex = assertThrows(TributiException.class,
                    () -> service.aggiornaPosizioneInCorso("NON_ESISTE"));

            assertSame(errore, ex);
        }

        @Test
        @DisplayName("DAO non registrato: ServiceNotFoundException")
        void daoNonTrovato() throws Exception {
            when(contesto.lookup(JNDI_POSIZIONE_DAO)).thenThrow(new NameNotFoundException("nessun binding"));

            Exception ex = assertThrows(Exception.class, () -> service.aggiornaPosizioneInCorso("IUV-1"));

            assertEquals("ServiceNotFoundException", ex.getClass().getSimpleName());
        }
    }

    @Nested
    @DisplayName("verificaPosizione")
    class Verifica {

        @Test
        @DisplayName("restituisce la lista del DAO così com'è")
        void restituisceLista() throws Exception {
            List<PosizioneDTO> lista = Arrays.asList(new PosizioneDTO(), new PosizioneDTO());
            when(posizioneDAO.findPosizioniByCodiceFiscale(CF)).thenReturn(lista);

            assertSame(lista, service.verificaPosizione(CF));
        }

        @Test
        @DisplayName("nessuna posizione: lista vuota")
        void listaVuota() throws Exception {
            when(posizioneDAO.findPosizioniByCodiceFiscale(CF)).thenReturn(Arrays.<PosizioneDTO>asList());

            assertTrue(service.verificaPosizione(CF).isEmpty());
        }

        @Test
        @DisplayName("DAO non registrato: ServiceNotFoundException")
        void daoNonTrovato() throws Exception {
            when(contesto.lookup(JNDI_POSIZIONE_DAO)).thenThrow(new NameNotFoundException("nessun binding"));

            Exception ex = assertThrows(Exception.class, () -> service.verificaPosizione(CF));

            assertEquals("ServiceNotFoundException", ex.getClass().getSimpleName());
        }
    }

    @Nested
    @DisplayName("createPagamento")
    class Creazione {

        @Test
        @DisplayName("passa al DAO i dati giusti: stesso importo come attuale e originale, stato DA_PAGARE, ente letto")
        void datiPassatiAlDao() throws Exception {
            Date prima = new Date();

            service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), "2026-12-31", "C_F839");

            ArgumentCaptor<Date> emissione = ArgumentCaptor.forClass(Date.class);
            ArgumentCaptor<Date> scadenza = ArgumentCaptor.forClass(Date.class);
            verify(posizioneDAO).createPagamento(anyString(), eq(CF), eq("MULTA"),
                    eq(new BigDecimal("80.00")), eq(new BigDecimal("80.00")), eq("DA_PAGARE"),
                    emissione.capture(), scadenza.capture(), same(ente));
            assertFalse(emissione.getValue().before(prima));
            assertEquals("2026-12-31", giorno(scadenza.getValue()));
        }

        @Test
        @DisplayName("restituisce lo IUV della posizione creata dal DAO")
        void restituisceIuv() throws Exception {
            String iuv = service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), "2026-12-31", "C_F839");

            ArgumentCaptor<String> iuvPassato = ArgumentCaptor.forClass(String.class);
            verify(posizioneDAO).createPagamento(iuvPassato.capture(), any(), any(), any(), any(), any(), any(), any(), any());
            assertEquals(iuvPassato.getValue(), iuv);
        }

        @Test
        @DisplayName("IUV generato: 17 cifre, cioè millisecondi attuali (13) + numero casuale (4)")
        void formatoIuv() throws Exception {
            long prima = System.currentTimeMillis();

            String iuv = service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), "2026-12-31", "C_F839");

            long dopo = System.currentTimeMillis();
            assertTrue(iuv.matches("\\d{17}"), iuv);
            long timestamp = Long.parseLong(iuv.substring(0, 13));
            assertTrue(timestamp >= prima && timestamp <= dopo);
        }

        @Test
        @DisplayName("IUV generato: entra nella colonna iuv (massimo 35 caratteri)")
        void lunghezzaIuv() throws Exception {
            String iuv = service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), "2026-12-31", "C_F839");

            assertTrue(iuv.length() <= 35);
        }

        @Test
        @DisplayName("ente inesistente: l'eccezione del DAO esce invariata, nessuna posizione creata")
        void enteInesistente() throws Exception {
            TributiException nonTrovato = new TributiException(
                    CodiciErrore.CODICE_NOTFOUND, CodiciErrore.MESSAGGIO_ENTE_NOTFOUND);
            when(enteDAO.getEnteByCodice("NON_ESISTE")).thenThrow(nonTrovato);

            TributiException ex = assertThrows(TributiException.class,
                    () -> service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), "2026-12-31", "NON_ESISTE"));

            assertSame(nonTrovato, ex);
            verifyNoInteractions(posizioneDAO);
        }

        @Test
        @DisplayName("data non valida: errore interno INT_001 (non DATA_001 / DATA_002), causa ParseException")
        void dataNonValida() {
            TributiException ex = assertThrows(TributiException.class,
                    () -> service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), "31/12/2026", "C_F839"));

            assertEquals(CodiciErrore.CODICE_ERRORE_INTERNO, ex.getCodice());
            assertTrue(ex.getCause() instanceof ParseException);
            verifyNoInteractions(posizioneDAO);
        }

        @Test
        @DisplayName("data null: errore interno INT_001, causa NullPointerException")
        void dataNull() {
            TributiException ex = assertThrows(TributiException.class,
                    () -> service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), null, "C_F839"));

            assertEquals(CodiciErrore.CODICE_ERRORE_INTERNO, ex.getCodice());
            assertTrue(ex.getCause() instanceof NullPointerException);
        }

        @ParameterizedTest(name = "\"{0}\" viene letta come {1}")
        @CsvSource({
                "2026-02-30,     2026-03-02",
                "2026-13-01,     2027-01-01",
                "2026-12-31xyz,  2026-12-31"
        })
        @DisplayName("date impossibili o sporche: accettate e corrette (SimpleDateFormat non rigoroso)")
        void dataNonRigorosa(String inserita, String letta) throws Exception {
            service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), inserita, "C_F839");

            ArgumentCaptor<Date> scadenza = ArgumentCaptor.forClass(Date.class);
            verify(posizioneDAO).createPagamento(any(), any(), any(), any(), any(), any(), any(), scadenza.capture(), any());
            assertEquals(letta, giorno(scadenza.getValue()));
        }

        @Test
        @DisplayName("scadenza nel passato: accettata (il controllo dei 30 giorni non è applicato)")
        void scadenzaPassata() throws Exception {
            String iuv = service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), "2000-01-01", "C_F839");

            assertNotNull(iuv);
        }

        @Test
        @DisplayName("importo null: accettato e passato al DAO (IMPO_001 non è usato)")
        void importoNull() throws Exception {
            String iuv = service.createPagamento(CF, "MULTA", null, "2026-12-31", "C_F839");

            assertNotNull(iuv);
            verify(posizioneDAO).createPagamento(any(), any(), any(), isNull(), isNull(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("il DAO restituisce null: errore interno INT_001, causa NullPointerException")
        void daoRestituisceNull() throws Exception {
            when(posizioneDAO.createPagamento(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(null);

            TributiException ex = assertThrows(TributiException.class,
                    () -> service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), "2026-12-31", "C_F839"));

            assertEquals(CodiciErrore.CODICE_ERRORE_INTERNO, ex.getCodice());
            assertTrue(ex.getCause() instanceof NullPointerException);
        }

        @Test
        @DisplayName("DAO non registrato: convertito in errore interno INT_001 (ServiceNotFoundException non esce)")
        void daoNonTrovato() throws Exception {
            when(contesto.lookup(JNDI_ENTE_DAO)).thenThrow(new NameNotFoundException("nessun binding"));

            TributiException ex = assertThrows(TributiException.class,
                    () -> service.createPagamento(CF, "MULTA", new BigDecimal("80.00"), "2026-12-31", "C_F839"));

            assertEquals(CodiciErrore.CODICE_ERRORE_INTERNO, ex.getCodice());
            assertEquals("ServiceNotFoundException", ex.getCause().getClass().getSimpleName());
        }
    }


    private static String giorno(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd").format(data);
    }
}