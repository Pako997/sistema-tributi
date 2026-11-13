package it.pa.tributi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.spi.InitialContextFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import it.pa.tributi.dao.IPagamentoDAO;
import it.pa.tributi.dao.IPosizioneDAO;
import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.dto.RicevutaDTO;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;
import it.pa.tributi.external.ISistemaPagamento;

@DisplayName("PagamentoService")
class PagamentoServiceTest {

    private static final String JNDI_POSIZIONE_DAO = "java:module/PosizioneDAO";
    private static final String JNDI_SISTEMA_PAGAMENTO = "java:module/SistemaPagamento";
    private static final String JNDI_PAGAMENTO_DAO = "java:module/PagamentoDAO";

    private static final String IUV = "IUV-1";
    private static final String TRX = "TRX-001";

    public static class FactoryDiTest implements InitialContextFactory {
        static Context contesto;

        @Override
        public Context getInitialContext(Hashtable<?, ?> env) {
            return contesto;
        }
    }

    private final PagamentoService service = new PagamentoService();

    private String factoryOriginale;
    private Context contesto;
    private IPosizioneDAO posizioneDAO;
    private ISistemaPagamento sistemaPagamento;
    private IPagamentoDAO pagamentoDAO;
    private PosizioneDTO posizione;

    @BeforeEach
    void installaContestoFinto() throws Exception {
        factoryOriginale = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, FactoryDiTest.class.getName());

        contesto = mock(Context.class);
        posizioneDAO = mock(IPosizioneDAO.class);
        sistemaPagamento = mock(ISistemaPagamento.class);
        pagamentoDAO = mock(IPagamentoDAO.class);
        FactoryDiTest.contesto = contesto;

        lenient().when(contesto.lookup(JNDI_POSIZIONE_DAO)).thenReturn(posizioneDAO);
        lenient().when(contesto.lookup(JNDI_SISTEMA_PAGAMENTO)).thenReturn(sistemaPagamento);
        lenient().when(contesto.lookup(JNDI_PAGAMENTO_DAO)).thenReturn(pagamentoDAO);

        posizione = posizione("DA_PAGARE", "100.00");
        lenient().when(posizioneDAO.findPosizioneByIUV(IUV)).thenReturn(posizione);
        lenient().when(sistemaPagamento.verificaPagamento(any(), any())).thenReturn(TRX);
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
    @DisplayName("Pagamento riuscito")
    class Riuscito {

        @Test
        @DisplayName("la ricevuta contiene i dati della posizione, l'id transazione e l'importo versato")
        void ricevuta() throws Exception {
            RicevutaDTO r = service.eseguiPagamento(IUV, 100.0);

            assertEquals("RSSMRA80A01F839X", r.getCodiceFiscale());
            assertEquals("TARI", r.getTipoTributo());
            assertEquals(IUV, r.getIuv());
            assertEquals(TRX, r.getIdTransazione());
            assertEquals(Double.valueOf(100.0), r.getImporto());
            assertEquals("C_F839", r.getCodiceEnte());
            assertEquals("Comune di Napoli", r.getEnteEmittente());
            assertEquals("tributi@comune.napoli.it", r.getContattoEnte());
        }

        @Test
        @DisplayName("la data di pagamento è il momento della chiamata")
        void dataPagamentoAdesso() throws Exception {
            long prima = System.currentTimeMillis();

            RicevutaDTO r = service.eseguiPagamento(IUV, 100.0);

            long dopo = System.currentTimeMillis();
            assertTrue(r.getDataPagamento().getTime() >= prima);
            assertTrue(r.getDataPagamento().getTime() <= dopo);
        }

        @Test
        @DisplayName("la stessa data finisce in posizione, archivio pagamenti e ricevuta")
        void stessaData() throws Exception {
            RicevutaDTO r = service.eseguiPagamento(IUV, 100.0);

            ArgumentCaptor<Date> dataPosizione = ArgumentCaptor.forClass(Date.class);
            ArgumentCaptor<Date> dataArchivio = ArgumentCaptor.forClass(Date.class);
            verify(posizioneDAO).aggiornaPosizionePagata(eq(IUV), dataPosizione.capture());
            verify(pagamentoDAO).registraPagamento(eq(IUV), eq(TRX), dataArchivio.capture());
            assertSame(r.getDataPagamento(), dataPosizione.getValue());
            assertSame(r.getDataPagamento(), dataArchivio.getValue());
        }

        @Test
        @DisplayName("al sistema di pagamento passa la posizione letta e l'importo versato")
        void chiamataSistemaPagamento() throws Exception {
            service.eseguiPagamento(IUV, 100.0);

            verify(sistemaPagamento).verificaPagamento(posizione, 100.0);
        }

        @Test
        @DisplayName("ordine: lettura, pagamento, posizione pagata, archivio")
        void ordine() throws Exception {
            service.eseguiPagamento(IUV, 100.0);

            InOrder ordine = inOrder(posizioneDAO, sistemaPagamento, pagamentoDAO);
            ordine.verify(posizioneDAO).findPosizioneByIUV(IUV);
            ordine.verify(sistemaPagamento).verificaPagamento(posizione, 100.0);
            ordine.verify(posizioneDAO).aggiornaPosizionePagata(eq(IUV), any(Date.class));
            ordine.verify(pagamentoDAO).registraPagamento(eq(IUV), eq(TRX), any(Date.class));
        }

        @Test
        @DisplayName("cerca i servizi con i nomi JNDI PosizioneDAO, SistemaPagamento e PagamentoDAO")
        void nomiJndi() throws Exception {
            service.eseguiPagamento(IUV, 100.0);

            verify(contesto).lookup(JNDI_POSIZIONE_DAO);
            verify(contesto).lookup(JNDI_SISTEMA_PAGAMENTO);
            verify(contesto).lookup(JNDI_PAGAMENTO_DAO);
        }
    }

    @Nested
    @DisplayName("Controlli assenti")
    class ControlliAssenti {

        @Test
        @DisplayName("importo versato diverso dal dovuto: pagamento accettato, in ricevuta l'importo versato")
        void importoNonConfrontato() throws Exception {
            RicevutaDTO r = service.eseguiPagamento(IUV, 1.0);

            assertEquals(Double.valueOf(1.0), r.getImporto());
            verify(posizioneDAO).aggiornaPosizionePagata(eq(IUV), any(Date.class));
        }

        @Test
        @DisplayName("posizione già PAGATA: viene pagata di nuovo")
        void giaPagata() throws Exception {
            when(posizioneDAO.findPosizioneByIUV(IUV)).thenReturn(posizione("PAGATA", "100.00"));

            service.eseguiPagamento(IUV, 100.0);

            verify(posizioneDAO).aggiornaPosizionePagata(eq(IUV), any(Date.class));
            verify(pagamentoDAO).registraPagamento(eq(IUV), eq(TRX), any(Date.class));
        }

        @Test
        @DisplayName("importo null: pagamento accettato, ricevuta con importo null")
        void importoNull() throws Exception {
            RicevutaDTO r = service.eseguiPagamento(IUV, null);

            assertNull(r.getImporto());
        }
    }

    @Nested
    @DisplayName("Errori")
    class Errori {

        @Test
        @DisplayName("IUV inesistente: l'eccezione del DAO esce invariata, nessun pagamento")
        void iuvInesistente() throws Exception {
            TributiException nonTrovato = new TributiException(
                    CodiciErrore.CODICE_IUV_PAGAMENTO_NOT_FOUND, CodiciErrore.MESSAGGIO_IUV_PAGAMENTO_NOT_FOUND);
            when(posizioneDAO.findPosizioneByIUV("NON_ESISTE")).thenThrow(nonTrovato);

            TributiException ex = assertThrows(TributiException.class,
                    () -> service.eseguiPagamento("NON_ESISTE", 100.0));

            assertSame(nonTrovato, ex);
            verifyNoInteractions(sistemaPagamento, pagamentoDAO);
        }

        @Test
        @DisplayName("pagamento non riuscito (id transazione null): PERR_001, nessun aggiornamento")
        void pagamentoNonRiuscito() throws Exception {
            when(sistemaPagamento.verificaPagamento(any(), any())).thenReturn(null);

            TributiException ex = assertThrows(TributiException.class,
                    () -> service.eseguiPagamento(IUV, 100.0));

            assertEquals(CodiciErrore.CODICE_PAGAMENTO_NON_RIUSCITO, ex.getCodice());
            assertEquals(CodiciErrore.MESSAGGIO_PAGAMENTO_NON_RIUSCITO, ex.getMessage());
            verify(posizioneDAO, never()).aggiornaPosizionePagata(any(), any());
            verifyNoInteractions(pagamentoDAO);
        }

        @Test
        @DisplayName("errore nel salvataggio in archivio: la posizione è già stata segnata PAGATA")
        void erroreArchivioDopoAggiornamento() throws Exception {
            TributiException errore = new TributiException(
                    CodiciErrore.CODICE_ERRORE_INTERNO, CodiciErrore.MESSAGGIO_ERRORE_INTERNO);
            doThrow(errore).when(pagamentoDAO).registraPagamento(any(), any(), any());

            TributiException ex = assertThrows(TributiException.class,
                    () -> service.eseguiPagamento(IUV, 100.0));

            assertSame(errore, ex);
            verify(posizioneDAO).aggiornaPosizionePagata(eq(IUV), any(Date.class));
        }

        @Test
        @DisplayName("sistema di pagamento non registrato: ServiceNotFoundException, nessun aggiornamento")
        void sistemaPagamentoNonTrovato() throws Exception {
            when(contesto.lookup(JNDI_SISTEMA_PAGAMENTO)).thenThrow(new NameNotFoundException("nessun binding"));

            Exception ex = assertThrows(Exception.class, () -> service.eseguiPagamento(IUV, 100.0));

            assertEquals("ServiceNotFoundException", ex.getClass().getSimpleName());
            verify(posizioneDAO, never()).aggiornaPosizionePagata(any(), any());
            verifyNoInteractions(pagamentoDAO);
        }
    }

    private static PosizioneDTO posizione(String stato, String importo) {
        PosizioneDTO p = new PosizioneDTO();
        p.setIuv(IUV);
        p.setCodiceFiscale("RSSMRA80A01F839X");
        p.setTipoTributo("TARI");
        p.setImporto(new BigDecimal(importo));
        p.setStato(stato);
        p.setCodiceEnte("C_F839");
        p.setEnteEmittente("Comune di Napoli");
        p.setContattoEnte("tributi@comune.napoli.it");
        return p;
    }
}