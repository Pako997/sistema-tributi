package it.pa.tributi.batch;

import it.pa.tributi.dao.IBatchNotificaDAO;
import it.pa.tributi.dao.INotificaDAO;
import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.external.ISistemaPostale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
public class NotificheBatchTest {

    private static final long UN_GIORNO_MS = 24L * 60 * 60 * 1000;
    private static final long UN_ORA_MS = 60L * 60 * 1000;

    @Mock
    private IBatchNotificaDAO batchDAO;
    @Mock private INotificaDAO notificaDAO;
    @Mock private ISistemaPostale sistemaPostale;

    @InjectMocks
    private NotificheBatch batch;

    private static Date scadutaDaGiorni(long giorni) {
        return new Date(System.currentTimeMillis() - giorni * UN_GIORNO_MS - UN_ORA_MS);
    }

    private static PosizioneDTO posizione(String iuv, String tipo, String importoOriginale, long giorniRitardo) {
        PosizioneDTO p = new PosizioneDTO();
        p.setIuv(iuv);
        p.setTipoTributo(tipo);
        p.setImportoOriginale(new BigDecimal(importoOriginale));
        p.setImporto(new BigDecimal(importoOriginale));
        p.setDataScadenza(scadutaDaGiorni(giorniRitardo));
        return p;
    }

    private static PosizioneDTO conIuv(String iuv) {
        PosizioneDTO p = new PosizioneDTO();
        p.setIuv(iuv);
        return p;
    }

    @Nested
    @DisplayName("Ricalcolo importo TARI")
    class Tari {

        @ParameterizedTest(name = "{0} giorni di ritardo -> {1}")
        @CsvSource({
                "1,   100.08",
                "10,  100.87",
                "14,  101.23",
                "15,  101.32",
                "30,  101.38",
                "31,  101.53",
                "90,  101.78",
                "91,  103.53"
        })
        void ricalcolaImportoPerFasciaDiRitardo(long giorni, String atteso) throws Exception {
            PosizioneDTO p = posizione("IUV-TARI", "TARI", "100.00", giorni);
            when(batchDAO.findPosizioniScadute()).thenReturn(Collections.singletonList(p));

            batch.eseguiNotifiche();

            verify(batchDAO).aggiornaImporto("IUV-TARI", new BigDecimal(atteso));
        }

        @Test
        @DisplayName("nessun aggiornamento se il ritardo è di 0 giorni")
        void nessunAggiornamentoSenzaRitardo() throws Exception {
            PosizioneDTO p = posizione("IUV-TARI", "TARI", "100.00", 0);
            when(batchDAO.findPosizioniScadute()).thenReturn(Collections.singletonList(p));

            batch.eseguiNotifiche();

            verify(batchDAO, never()).aggiornaImporto(any(), any());
        }

        @Test
        @DisplayName("nessun aggiornamento se l'importo è già quello calcolato")
        void nessunAggiornamentoSeImportoInvariato() throws Exception {
            PosizioneDTO p = posizione("IUV-TARI", "TARI", "100.00", 10);
            p.setImporto(new BigDecimal("100.87"));
            when(batchDAO.findPosizioniScadute()).thenReturn(Collections.singletonList(p));

            batch.eseguiNotifiche();

            verify(batchDAO, never()).aggiornaImporto(any(), any());
        }
    }

    @Nested
    @DisplayName("Ricalcolo importo MULTA")
    class Multa {

        @ParameterizedTest(name = "{0} giorni di ritardo -> {1}")
        @CsvSource({
                "61,   200.00",  // primo giorno di raddoppio
                "239,  200.00",  // 179 giorni oltre i 60: ancora 0 semestri
                "240,  220.00",  // primo semestre intero
                "420,  240.00",  // 2 semestri
                "1140, 320.00",  // 6 semestri = 60%, esattamente il tetto
                "1320, 320.00"   // 7 semestri: tetto applicato
        })
        void ricalcolaImportoOltre60Giorni(long giorni, String atteso) throws Exception {
            PosizioneDTO p = posizione("IUV-MULTA", "MULTA", "100.00", giorni);
            when(batchDAO.findPosizioniScadute()).thenReturn(Collections.singletonList(p));

            batch.eseguiNotifiche();

            verify(batchDAO).aggiornaImporto("IUV-MULTA", new BigDecimal(atteso));
        }

        @Test
        @DisplayName("nessun aggiornamento fino a 60 giorni (lo sconto 30% NON è gestito dal batch)")
        void nessunAggiornamentoEntro60Giorni() throws Exception {
            PosizioneDTO p = posizione("IUV-MULTA", "MULTA", "100.00", 60);
            when(batchDAO.findPosizioniScadute()).thenReturn(Collections.singletonList(p));

            batch.eseguiNotifiche();

            verify(batchDAO, never()).aggiornaImporto(any(), any());
        }
    }

    @Test
    @DisplayName("tipi tributo diversi da TARI e MULTA vengono ignorati")
    void tipoTributoSconosciutoIgnorato() throws Exception {
        PosizioneDTO p = posizione("IUV-IMU", "IMU", "100.00", 100);
        when(batchDAO.findPosizioniScadute()).thenReturn(Collections.singletonList(p));

        batch.eseguiNotifiche();

        verify(batchDAO, never()).aggiornaImporto(any(), any());
    }

    @Nested
    @DisplayName("Invio notifiche")
    class Notifiche {

        @Test
        @DisplayName("blocco emissione: inviato in un'unica chiamata e registrato posizione per posizione")
        void bloccoEmissione() throws Exception {
            List<PosizioneDTO> blocco = Arrays.asList(conIuv("IUV-1"), conIuv("IUV-2"));
            when(batchDAO.findPosizioniDaNotificareEmissione()).thenReturn(blocco);

            batch.eseguiNotifiche();

            verify(sistemaPostale).inviaBlocco(blocco);
            verify(notificaDAO).registraNotifica("IUV-1", "EMISSIONE");
            verify(notificaDAO).registraNotifica("IUV-2", "EMISSIONE");
        }

        @Test
        @DisplayName("sollecito 15: interroga il DAO con intervallo 0-15")
        void sollecito15() throws Exception {
            List<PosizioneDTO> blocco = Collections.singletonList(conIuv("IUV-15"));
            when(batchDAO.findPosizioniDaNotificare(0, 15, "SOLLECITO15")).thenReturn(blocco);

            batch.eseguiNotifiche();

            verify(sistemaPostale).inviaBlocco(blocco);
            verify(notificaDAO).registraNotifica("IUV-15", "SOLLECITO15");
        }

        @Test
        @DisplayName("sollecito 30: interroga il DAO con intervallo 15-30")
        void sollecito30() throws Exception {
            List<PosizioneDTO> blocco = Collections.singletonList(conIuv("IUV-30"));
            lenient().when(batchDAO.findPosizioniDaNotificare(15, 30, "SOLLECITO30")).thenReturn(blocco);
            batch.eseguiNotifiche();

            verify(sistemaPostale).inviaBlocco(blocco);
            verify(notificaDAO).registraNotifica("IUV-30", "SOLLECITO30");
        }

        @Test
        @DisplayName("blocchi vuoti: nessun invio e nessuna registrazione")
        void blocchiVuoti() throws Exception {
            // Mockito restituisce liste vuote di default per i metodi non stubbati
            batch.eseguiNotifiche();

            verifyNoInteractions(sistemaPostale, notificaDAO);
        }

        @Test
        @DisplayName("ordine: prima si aggiornano gli importi, poi si inviano e registrano le notifiche")
        void ordineOperazioni() throws Exception {
            PosizioneDTO scaduta = posizione("IUV-1", "MULTA", "100.00", 61);
            List<PosizioneDTO> blocco = Collections.singletonList(conIuv("IUV-1"));
            when(batchDAO.findPosizioniScadute()).thenReturn(Collections.singletonList(scaduta));
            when(batchDAO.findPosizioniDaNotificareEmissione()).thenReturn(blocco);

            batch.eseguiNotifiche();

            InOrder ordine = inOrder(batchDAO, sistemaPostale, notificaDAO);
            ordine.verify(batchDAO).findPosizioniScadute();
            ordine.verify(batchDAO).aggiornaImporto(eq("IUV-1"), any());
            ordine.verify(sistemaPostale).inviaBlocco(blocco);
            ordine.verify(notificaDAO).registraNotifica("IUV-1", "EMISSIONE");
        }
    }

    @Nested
    @DisplayName("Gestione errori")
    class Errori {

        @Test
        @DisplayName("errore nel recupero delle scadute: eccezione inghiottita, nessuna notifica inviata")
        void erroreRecuperoScadute() throws Exception {
            when(batchDAO.findPosizioniScadute()).thenThrow(new RuntimeException("DB non disponibile"));

            assertDoesNotThrow(() -> batch.eseguiNotifiche());

            verify(batchDAO, never()).findPosizioniDaNotificareEmissione();
            verifyNoInteractions(sistemaPostale, notificaDAO);
        }

        @Test
        @DisplayName("errore su una posizione: le successive non vengono aggiornate e le notifiche saltano")
        void erroreSuUnaPosizioneBloccaTutto() throws Exception {
            PosizioneDTO p1 = posizione("IUV-1", "MULTA", "100.00", 61);
            PosizioneDTO p2 = posizione("IUV-2", "MULTA", "100.00", 61);
            when(batchDAO.findPosizioniScadute()).thenReturn(Arrays.asList(p1, p2));
            doThrow(new RuntimeException("lock su riga")).when(batchDAO).aggiornaImporto(eq("IUV-1"), any());

            batch.eseguiNotifiche();

            verify(batchDAO, never()).aggiornaImporto(eq("IUV-2"), any());
            verifyNoInteractions(sistemaPostale, notificaDAO);
        }

        @Test
        @DisplayName("errore in registrazione: la posta è già partita ma la notifica non risulta registrata")
        void erroreRegistrazioneDopoInvio() throws Exception {
            List<PosizioneDTO> emissione = Arrays.asList(conIuv("IUV-1"), conIuv("IUV-2"));
            when(batchDAO.findPosizioniDaNotificareEmissione()).thenReturn(emissione);
            lenient().when(batchDAO.findPosizioniDaNotificare(0, 15, "SOLLECITO15"))
                    .thenReturn(Collections.singletonList(conIuv("IUV-15")));
            doThrow(new RuntimeException("errore insert"))
                    .when(notificaDAO).registraNotifica("IUV-1", "EMISSIONE");

            batch.eseguiNotifiche();

            verify(sistemaPostale, times(1)).inviaBlocco(anyList()); // solo l'emissione è partita
            verify(notificaDAO, never()).registraNotifica("IUV-2", "EMISSIONE");
        }
    }
}
