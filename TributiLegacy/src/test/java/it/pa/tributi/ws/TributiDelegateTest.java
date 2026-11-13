package it.pa.tributi.ws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import javax.management.ServiceNotFoundException;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import it.pa.tributi.context.UtenteContext;
import it.pa.tributi.dao.INotificaDAO;
import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.dto.RicevutaDTO;
import it.pa.tributi.entity.Notifica;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;
import it.pa.tributi.service.IOperatoreService;
import it.pa.tributi.service.IPagamentoService;
import it.pa.tributi.service.IPosizioneService;

@DisplayName("TributiDelegate")
class TributiDelegateTest {

    private static final String JNDI_NOTIFICA_DAO = "java:module/NotificaDAO";
    private static final String JNDI_POSIZIONE_SERVICE = "java:module/PosizioneService";
    private static final String JNDI_PAGAMENTO_SERVICE = "java:module/PagamentoService";
    private static final String JNDI_OPERATORE_SERVICE = "java:module/OperatoreService";

    private static final String CF_OPERATORE = "VRDLGI75B02H501Y";
    private static final String CF_CITTADINO = "RSSMRA80A01F839X";
    private static final String CF_ALTRO = "BNCGNN90C03F205Z";

    public static class FactoryDiTest implements InitialContextFactory {
        static Context contesto;

        @Override
        public Context getInitialContext(Hashtable<?, ?> env) {
            return contesto;
        }
    }

    private final TributiDelegate delegate = new TributiDelegate();

    private String factoryOriginale;
    private Context contesto;
    private INotificaDAO notificaDAO;
    private IPosizioneService posizioneService;
    private IPagamentoService pagamentoService;
    private IOperatoreService operatoreService;

    @BeforeEach
    void installaContestoFinto() throws Exception {
        factoryOriginale = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, FactoryDiTest.class.getName());

        contesto = mock(Context.class);
        notificaDAO = mock(INotificaDAO.class);
        posizioneService = mock(IPosizioneService.class);
        pagamentoService = mock(IPagamentoService.class);
        operatoreService = mock(IOperatoreService.class);
        FactoryDiTest.contesto = contesto;

        lenient().when(contesto.lookup(JNDI_NOTIFICA_DAO)).thenReturn(notificaDAO);
        lenient().when(contesto.lookup(JNDI_POSIZIONE_SERVICE)).thenReturn(posizioneService);
        lenient().when(contesto.lookup(JNDI_PAGAMENTO_SERVICE)).thenReturn(pagamentoService);
        lenient().when(contesto.lookup(JNDI_OPERATORE_SERVICE)).thenReturn(operatoreService);

        lenient().when(operatoreService.getRuolo(CF_OPERATORE)).thenReturn("OPERATORE");
        lenient().when(operatoreService.getRuolo(CF_CITTADINO)).thenReturn("CITTADINO");
    }

    @AfterEach
    void ripristina() {
        UtenteContext.clear();
        if (factoryOriginale == null) {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        } else {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, factoryOriginale);
        }
        FactoryDiTest.contesto = null;
    }


    @Nested
    @DisplayName("visualNotifiche")
    class VisualNotifiche {

        @Test
        @DisplayName("restituisce la lista del DAO così com'è")
        void restituisceLista() throws Exception {
            List<Notifica> lista = Arrays.asList(new Notifica(), new Notifica());
            when(notificaDAO.visualNotifica()).thenReturn(lista);

            assertSame(lista, delegate.visualNotifiche());
        }

        @Test
        @DisplayName("non controlla l'autenticazione: funziona anche senza utente")
        void senzaAutenticazione() throws Exception {
            delegate.visualNotifiche();

            verify(notificaDAO).visualNotifica();
        }

        @Test
        @DisplayName("DAO non registrato: ServiceNotFoundException, non convertita")
        void daoNonTrovato() throws Exception {
            when(contesto.lookup(JNDI_NOTIFICA_DAO)).thenThrow(new NameNotFoundException("nessun binding"));

            Exception ex = assertThrows(Exception.class, () -> delegate.visualNotifiche());

            assertEquals("ServiceNotFoundException", ex.getClass().getSimpleName());
        }
    }


    @Nested
    @DisplayName("eseguiPagamento")
    class EseguiPagamento {

        @Test
        @DisplayName("segna la posizione IN_CORSO, poi paga e restituisce la ricevuta del servizio")
        void pagamento() throws Exception {
            RicevutaDTO ricevuta = new RicevutaDTO();
            when(pagamentoService.eseguiPagamento("IUV-1", 100.0)).thenReturn(ricevuta);

            assertSame(ricevuta, delegate.eseguiPagamento("IUV-1", 100.0));

            InOrder ordine = inOrder(posizioneService, pagamentoService);
            ordine.verify(posizioneService).aggiornaPosizioneInCorso("IUV-1");
            ordine.verify(pagamentoService).eseguiPagamento("IUV-1", 100.0);
        }

        @Test
        @DisplayName("non controlla l'autenticazione: funziona anche senza utente")
        void senzaAutenticazione() throws Exception {
            delegate.eseguiPagamento("IUV-1", 100.0);

            verify(pagamentoService).eseguiPagamento("IUV-1", 100.0);
        }

        @Test
        @DisplayName("errore nel segnare IN_CORSO: eccezione invariata, nessun pagamento")
        void erroreInCorso() throws Exception {
            TributiException nonTrovato = new TributiException(
                    CodiciErrore.CODICE_IUV_PAGAMENTO_NOT_FOUND, CodiciErrore.MESSAGGIO_IUV_PAGAMENTO_NOT_FOUND);
            doThrow(nonTrovato).when(posizioneService).aggiornaPosizioneInCorso("NON_ESISTE");

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.eseguiPagamento("NON_ESISTE", 100.0));

            assertSame(nonTrovato, ex);
            verifyNoInteractions(pagamentoService);
        }

        @Test
        @DisplayName("pagamento fallito: eccezione invariata, la posizione resta IN_CORSO")
        void pagamentoFallito() throws Exception {
            TributiException fallito = new TributiException(
                    CodiciErrore.CODICE_PAGAMENTO_NON_RIUSCITO, CodiciErrore.MESSAGGIO_PAGAMENTO_NON_RIUSCITO);
            when(pagamentoService.eseguiPagamento("IUV-1", 100.0)).thenThrow(fallito);

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.eseguiPagamento("IUV-1", 100.0));

            assertSame(fallito, ex);
            verify(posizioneService).aggiornaPosizioneInCorso("IUV-1");
        }

        @Test
        @DisplayName("servizio non registrato: INT_001, messaggio con il dettaglio, causa conservata")
        void servizioNonTrovato() throws Exception {
            when(contesto.lookup(JNDI_PAGAMENTO_SERVICE)).thenThrow(new NameNotFoundException("nessun binding"));

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.eseguiPagamento("IUV-1", 100.0));

            assertEquals(CodiciErrore.CODICE_ERRORE_INTERNO, ex.getCodice());
            assertEquals("Errore interno : Non trovato: " + IPagamentoService.class.getName(), ex.getMessage());
            assertEquals("ServiceNotFoundException", ex.getCause().getClass().getSimpleName());
        }

        @Test
        @DisplayName("errore imprevisto: INT_001 con il messaggio originale accodato")
        void erroreImprevisto() throws Exception {
            when(pagamentoService.eseguiPagamento("IUV-1", 100.0))
                    .thenThrow(new IllegalStateException("connessione persa"));

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.eseguiPagamento("IUV-1", 100.0));

            assertEquals(CodiciErrore.CODICE_ERRORE_INTERNO, ex.getCodice());
            assertEquals("Errore interno : connessione persa", ex.getMessage());
            assertTrue(ex.getCause() instanceof IllegalStateException);
        }
    }

    @Nested
    @DisplayName("verificaPosizione")
    class VerificaPosizione {

        @Test
        @DisplayName("operatore: vede le posizioni del codice fiscale richiesto")
        void operatore() throws Exception {
            UtenteContext.set(CF_OPERATORE);
            List<PosizioneDTO> lista = Arrays.asList(new PosizioneDTO());
            when(posizioneService.verificaPosizione(CF_ALTRO)).thenReturn(lista);

            assertSame(lista, delegate.verificaPosizione(CF_ALTRO));
        }

        @Test
        @DisplayName("cittadino che chiede le proprie posizioni: le vede")
        void cittadinoProprie() throws Exception {
            UtenteContext.set(CF_CITTADINO);
            List<PosizioneDTO> lista = Arrays.asList(new PosizioneDTO());
            when(posizioneService.verificaPosizione(CF_CITTADINO)).thenReturn(lista);

            assertSame(lista, delegate.verificaPosizione(CF_CITTADINO));
        }

        @Test
        @DisplayName("cittadino che chiede le posizioni di un altro: riceve le proprie, senza errore")
        void cittadinoAltrui() throws Exception {
            UtenteContext.set(CF_CITTADINO);
            List<PosizioneDTO> proprie = Arrays.asList(new PosizioneDTO());
            when(posizioneService.verificaPosizione(CF_CITTADINO)).thenReturn(proprie);

            assertSame(proprie, delegate.verificaPosizione(CF_ALTRO));
            verify(posizioneService, never()).verificaPosizione(CF_ALTRO);
        }

        @Test
        @DisplayName("il ruolo si calcola sul codice fiscale autenticato, non su quello richiesto")
        void ruoloDaUtenteAutenticato() throws Exception {
            UtenteContext.set(CF_CITTADINO);

            delegate.verificaPosizione(CF_OPERATORE);

            verify(operatoreService).getRuolo(CF_CITTADINO);
            verify(operatoreService, never()).getRuolo(CF_OPERATORE);
        }

        @ParameterizedTest(name = "codice fiscale richiesto \"{0}\": PMISS_001")
        @ValueSource(strings = {"", "   "})
        void codiceFiscaleVuoto(String cf) {
            UtenteContext.set(CF_OPERATORE);

            TributiException ex = assertThrows(TributiException.class, () -> delegate.verificaPosizione(cf));

            assertEquals(CodiciErrore.CODICE_PARAMETRI_MANCANTI, ex.getCodice());
            verifyNoInteractions(contesto);
        }

        @Test
        @DisplayName("codice fiscale richiesto null: PMISS_001")
        void codiceFiscaleNull() {
            UtenteContext.set(CF_OPERATORE);

            TributiException ex = assertThrows(TributiException.class, () -> delegate.verificaPosizione(null));

            assertEquals(CodiciErrore.CODICE_PARAMETRI_MANCANTI, ex.getCodice());
        }

        @Test
        @DisplayName("utente non autenticato: AUTH_000, nessun servizio chiamato")
        void nonAutenticato() {
            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.verificaPosizione(CF_CITTADINO));

            assertEquals(CodiciErrore.CODICE_AUTENTICAZIONE_MANCANTE, ex.getCodice());
            verifyNoInteractions(contesto);
        }

        @Test
        @DisplayName("utente autenticato con codice fiscale vuoto: AUTH_000")
        void autenticatoVuoto() {
            UtenteContext.set("   ");

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.verificaPosizione(CF_CITTADINO));

            assertEquals(CodiciErrore.CODICE_AUTENTICAZIONE_MANCANTE, ex.getCodice());
        }

        @Test
        @DisplayName("parametro mancante e utente non autenticato: prevale PMISS_001")
        void ordineControlli() {
            TributiException ex = assertThrows(TributiException.class, () -> delegate.verificaPosizione(""));

            assertEquals(CodiciErrore.CODICE_PARAMETRI_MANCANTI, ex.getCodice());
        }

        @Test
        @DisplayName("servizio non registrato: INT_001")
        void servizioNonTrovato() throws Exception {
            UtenteContext.set(CF_CITTADINO);
            when(contesto.lookup(JNDI_OPERATORE_SERVICE)).thenThrow(new NameNotFoundException("nessun binding"));

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.verificaPosizione(CF_CITTADINO));

            assertEquals(CodiciErrore.CODICE_ERRORE_INTERNO, ex.getCodice());
        }
    }


    @Nested
    @DisplayName("registraPagamento")
    class RegistraPagamento {

        @Test
        @DisplayName("operatore con dati validi: crea la posizione e restituisce lo IUV")
        void operatore() throws Exception {
            UtenteContext.set(CF_OPERATORE);
            String scadenza = traGiorni(40);
            when(posizioneService.createPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), scadenza, "C_F839"))
                    .thenReturn("12345678901234567");

            String iuv = delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), scadenza, "C_F839");

            assertEquals("12345678901234567", iuv);
        }

        @Test
        @DisplayName("cittadino: FORB_001, nessuna posizione creata")
        void cittadino() throws Exception {
            UtenteContext.set(CF_CITTADINO);

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), traGiorni(40), "C_F839"));

            assertEquals(CodiciErrore.CODICE_FORBIDDEM, ex.getCodice());
            verify(posizioneService, never()).createPagamento(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("cittadino con scadenza troppo vicina: DATA_003 invece di FORB_001 (la data si controlla prima del ruolo)")
        void cittadinoScadenzaVicina() {
            UtenteContext.set(CF_CITTADINO);

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), traGiorni(5), "C_F839"));

            assertEquals(CodiciErrore.CODICE_SCADENZA_NON_VALIDA, ex.getCodice());
        }

        @ParameterizedTest(name = "parametro mancante: {0}")
        @ValueSource(strings = {
                "codiceFiscale null", "codiceFiscale vuoto", "tipoTributo null", "tipoTributo vuoto",
                "importo null", "dataScadenza null", "dataScadenza vuota", "codiceEnte null", "codiceEnte vuoto"
        })
        void parametroMancante(String caso) {
            UtenteContext.set(CF_OPERATORE);
            String cf = CF_CITTADINO;
            String tipo = "MULTA";
            BigDecimal importo = new BigDecimal("80.00");
            String data = traGiorni(40);
            String ente = "C_F839";
            switch (caso) {
                case "codiceFiscale null": cf = null; break;
                case "codiceFiscale vuoto": cf = "  "; break;
                case "tipoTributo null": tipo = null; break;
                case "tipoTributo vuoto": tipo = ""; break;
                case "importo null": importo = null; break;
                case "dataScadenza null": data = null; break;
                case "dataScadenza vuota": data = " "; break;
                case "codiceEnte null": ente = null; break;
                case "codiceEnte vuoto": ente = ""; break;
                default: throw new IllegalArgumentException(caso);
            }
            final String cfF = cf, tipoF = tipo, dataF = data, enteF = ente;
            final BigDecimal importoF = importo;

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(cfF, tipoF, importoF, dataF, enteF));

            assertEquals(CodiciErrore.CODICE_PARAMETRI_MANCANTI, ex.getCodice());
            verifyNoInteractions(contesto);
        }

        @ParameterizedTest(name = "importo {0}: IMPO_001")
        @ValueSource(strings = {"0", "0.00", "-10.00"})
        void importoNonPositivo(String importo) {
            UtenteContext.set(CF_OPERATORE);

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal(importo), traGiorni(40), "C_F839"));

            assertEquals(CodiciErrore.CODICE_IMPORTO_NULLO, ex.getCodice());
        }

        @ParameterizedTest(name = "data \"{0}\": DATA_001")
        @ValueSource(strings = {"31/12/2026", "2026-1-01", "2026-12-1", "20261231", "2026-12-31 10:00"})
        void dataSformata(String data) {
            UtenteContext.set(CF_OPERATORE);

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), data, "C_F839"));

            assertEquals(CodiciErrore.CODICE_DATA_SFORMATA, ex.getCodice());
        }

        @ParameterizedTest(name = "data \"{0}\": DATA_002, causa ParseException")
        @ValueSource(strings = {"2099-02-30", "2099-13-01", "2099-00-10"})
        void dataImpossibile(String data) {
            UtenteContext.set(CF_OPERATORE);

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), data, "C_F839"));

            assertEquals(CodiciErrore.CODICE_DATA_NON_PARSABILE, ex.getCodice());
            assertTrue(ex.getCause() instanceof ParseException);
        }

        @ParameterizedTest(name = "scadenza fra {0} giorni: DATA_003")
        @CsvSource({"-1", "0", "29"})
        void scadenzaTroppoVicina(int giorni) throws ServiceNotFoundException, TributiException {
            UtenteContext.set(CF_OPERATORE);

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), traGiorni(giorni), "C_F839"));

            assertEquals(CodiciErrore.CODICE_SCADENZA_NON_VALIDA, ex.getCodice());
            verify(posizioneService, never()).createPagamento(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("scadenza fra esattamente 30 giorni: accettata")
        void scadenzaAlLimite() throws Exception {
            UtenteContext.set(CF_OPERATORE);
            String scadenza = traGiorni(30);

            delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), scadenza, "C_F839");

            verify(posizioneService).createPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), scadenza, "C_F839");
        }

        @Test
        @DisplayName("utente non autenticato: AUTH_000")
        void nonAutenticato() {
            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), traGiorni(40), "C_F839"));

            assertEquals(CodiciErrore.CODICE_AUTENTICAZIONE_MANCANTE, ex.getCodice());
        }

        @Test
        @DisplayName("parametro mancante e utente non autenticato: prevale PMISS_001")
        void ordineControlli() {
            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(null, "MULTA", new BigDecimal("80.00"), traGiorni(40), "C_F839"));

            assertEquals(CodiciErrore.CODICE_PARAMETRI_MANCANTI, ex.getCodice());
        }

        @Test
        @DisplayName("errore del servizio (ente inesistente): eccezione invariata")
        void erroreServizio() throws Exception {
            UtenteContext.set(CF_OPERATORE);
            TributiException nonTrovato = new TributiException(
                    CodiciErrore.CODICE_NOTFOUND, CodiciErrore.MESSAGGIO_ENTE_NOTFOUND);
            when(posizioneService.createPagamento(any(), any(), any(), any(), eq("NON_ESISTE"))).thenThrow(nonTrovato);

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), traGiorni(40), "NON_ESISTE"));

            assertSame(nonTrovato, ex);
        }

        @Test
        @DisplayName("servizio non registrato: INT_001")
        void servizioNonTrovato() throws Exception {
            UtenteContext.set(CF_OPERATORE);
            when(contesto.lookup(JNDI_POSIZIONE_SERVICE)).thenThrow(new NameNotFoundException("nessun binding"));

            TributiException ex = assertThrows(TributiException.class,
                    () -> delegate.registraPagamento(CF_CITTADINO, "MULTA", new BigDecimal("80.00"), traGiorni(40), "C_F839"));

            assertEquals(CodiciErrore.CODICE_ERRORE_INTERNO, ex.getCodice());
        }
    }


    /** Data di oggi + giorni, nel formato yyyy-MM-dd accettato dal web service. */
    private static String traGiorni(int giorni) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, giorni);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
}