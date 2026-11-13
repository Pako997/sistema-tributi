package it.pa.tributi.tributiservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.pa.tributi.tributiservice.dto.PosizioneDTO;
import it.pa.tributi.tributiservice.dto.RicevutaDTO;
import it.pa.tributi.tributiservice.entity.ArchivioPagamento;
import it.pa.tributi.tributiservice.entity.Posizione;
import it.pa.tributi.tributiservice.exception.ErroreTributi;
import it.pa.tributi.tributiservice.exception.TributiException;
import it.pa.tributi.tributiservice.external.SistemaPagamento;
import it.pa.tributi.tributiservice.mapper.PosizioneMapper;
import it.pa.tributi.tributiservice.mapper.RicevutaMapper;
import it.pa.tributi.tributiservice.repository.ArchivioRepository;
import it.pa.tributi.tributiservice.repository.PosizioneRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagamentoService")
class PagamentoServiceTest {

    private static final String IUV = "IUV-1";
    private static final String TRX = "TRX-001";
    private static final BigDecimal VERSATO = new BigDecimal("100.00");

    @Mock private PosizioneRepository posizioneRepository;
    @Mock private ArchivioRepository archivioRepository;
    @Mock private SistemaPagamento sistemaPagamento;
    @Mock private RicevutaMapper ricevutaMapper;
    @Mock private PosizioneMapper posizioneMapper;

    @Mock private PosizioneDTO posizioneDTO;
    @Mock private RicevutaDTO ricevuta;

    private PagamentoService service;
    private Posizione posizione;

    @BeforeEach
    void crea() {
        service = new PagamentoService(posizioneRepository, archivioRepository,
                sistemaPagamento, ricevutaMapper, posizioneMapper);

        posizione = new Posizione();
        posizione.setIuv(IUV);
        posizione.setStato("DA_PAGARE");
        posizione.setImporto(new BigDecimal("100.00"));
    }

    /** Prepara il percorso fino all'autorizzazione del pagamento. */
    private void pagamentoAutorizzato(String idTransazione) {
        when(posizioneRepository.findByIuv(IUV)).thenReturn(Optional.of(posizione));
        when(posizioneMapper.toDTO(posizione)).thenReturn(posizioneDTO);
        when(sistemaPagamento.autorizza(posizioneDTO, VERSATO)).thenReturn(idTransazione);
    }

    @Nested
    @DisplayName("Pagamento riuscito")
    class Riuscito {

        @BeforeEach
        void prepara() {
            pagamentoAutorizzato(TRX);
            when(ricevutaMapper.toRicevuta(posizione, TRX, VERSATO, LocalDate.now())).thenReturn(ricevuta);
        }

        @Test
        @DisplayName("restituisce la ricevuta costruita dal mapper con id transazione, importo versato e data di oggi")
        void ricevuta() {
            assertThat(service.eseguiPagamento(IUV, VERSATO)).isSameAs(ricevuta);
        }

        @Test
        @DisplayName("la posizione diventa PAGATA con la data di pagamento di oggi")
        void posizionePagata() {
            service.eseguiPagamento(IUV, VERSATO);

            assertThat(posizione.getStato()).isEqualTo("PAGATA");
            assertThat(posizione.getDataPagamento()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("salva in archivio iuv, id transazione e data di oggi")
        void archivio() {
            service.eseguiPagamento(IUV, VERSATO);

            ArgumentCaptor<ArchivioPagamento> captor = ArgumentCaptor.forClass(ArchivioPagamento.class);
            verify(archivioRepository).save(captor.capture());
            ArchivioPagamento salvato = captor.getValue();
            assertThat(salvato.getIuv()).isEqualTo(IUV);
            assertThat(salvato.getIdTransazione()).isEqualTo(TRX);
            assertThat(salvato.getDataPagamento()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("al sistema di pagamento passa il DTO della posizione e l'importo versato")
        void autorizzazione() {
            service.eseguiPagamento(IUV, VERSATO);

            verify(sistemaPagamento).autorizza(posizioneDTO, VERSATO);
        }

        @Test
        @DisplayName("ordine: lettura, conversione, autorizzazione, archivio, ricevuta")
        void ordine() {
            service.eseguiPagamento(IUV, VERSATO);

            InOrder ordine = inOrder(posizioneRepository, posizioneMapper, sistemaPagamento,
                    archivioRepository, ricevutaMapper);
            ordine.verify(posizioneRepository).findByIuv(IUV);
            ordine.verify(posizioneMapper).toDTO(posizione);
            ordine.verify(sistemaPagamento).autorizza(posizioneDTO, VERSATO);
            ordine.verify(archivioRepository).save(any(ArchivioPagamento.class));
            ordine.verify(ricevutaMapper).toRicevuta(posizione, TRX, VERSATO, LocalDate.now());
        }

        @Test
        @DisplayName("non chiama save sulla posizione: il salvataggio si affida alla transazione")
        void nessunSavePosizione() {
            service.eseguiPagamento(IUV, VERSATO);

            verify(posizioneRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Controlli assenti")
    class ControlliAssenti {

        @Test
        @DisplayName("importo versato diverso dal dovuto: pagamento accettato")
        void importoDiverso() {
            BigDecimal pocoVersato = new BigDecimal("1.00");
            when(posizioneRepository.findByIuv(IUV)).thenReturn(Optional.of(posizione));
            when(posizioneMapper.toDTO(posizione)).thenReturn(posizioneDTO);
            when(sistemaPagamento.autorizza(posizioneDTO, pocoVersato)).thenReturn(TRX);

            service.eseguiPagamento(IUV, pocoVersato);

            assertThat(posizione.getStato()).isEqualTo("PAGATA");
            verify(ricevutaMapper).toRicevuta(posizione, TRX, pocoVersato, LocalDate.now());
        }

        @Test
        @DisplayName("posizione già PAGATA: viene pagata di nuovo e archiviata una seconda volta")
        void giaPagata() {
            posizione.setStato("PAGATA");
            pagamentoAutorizzato(TRX);

            service.eseguiPagamento(IUV, VERSATO);

            verify(sistemaPagamento).autorizza(posizioneDTO, VERSATO);
            verify(archivioRepository).save(any(ArchivioPagamento.class));
        }
    }


    @Nested
    @DisplayName("Errori")
    class Errori {

        @Test
        @DisplayName("IUV inesistente: IUV_NON_TROVATO, nessun pagamento")
        void iuvNonTrovato() {
            when(posizioneRepository.findByIuv("NON_ESISTE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.eseguiPagamento("NON_ESISTE", VERSATO))
                    .isInstanceOf(TributiException.class)
                    .extracting("errore").isEqualTo(ErroreTributi.IUV_NON_TROVATO);

            verifyNoInteractions(posizioneMapper, sistemaPagamento, archivioRepository, ricevutaMapper);
        }

        @Test
        @DisplayName("pagamento non autorizzato (id transazione null): PAGAMENTO_NON_RIUSCITO, posizione invariata")
        void pagamentoNonRiuscito() {
            pagamentoAutorizzato(null);

            assertThatThrownBy(() -> service.eseguiPagamento(IUV, VERSATO))
                    .isInstanceOf(TributiException.class)
                    .extracting("errore").isEqualTo(ErroreTributi.PAGAMENTO_NON_RIUSCITO);

            assertThat(posizione.getStato()).isEqualTo("DA_PAGARE");
            assertThat(posizione.getDataPagamento()).isNull();
            verifyNoInteractions(archivioRepository, ricevutaMapper);
        }

        @Test
        @DisplayName("errore del sistema di pagamento: l'eccezione esce, posizione invariata")
        void erroreSistemaPagamento() {
            when(posizioneRepository.findByIuv(IUV)).thenReturn(Optional.of(posizione));
            when(posizioneMapper.toDTO(posizione)).thenReturn(posizioneDTO);
            when(sistemaPagamento.autorizza(posizioneDTO, VERSATO))
                    .thenThrow(new IllegalStateException("servizio non raggiungibile"));

            assertThatThrownBy(() -> service.eseguiPagamento(IUV, VERSATO))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(posizione.getStato()).isEqualTo("DA_PAGARE");
            verifyNoInteractions(archivioRepository);
        }

        @Test
        @DisplayName("errore nel salvataggio in archivio: l'eccezione esce, nessuna ricevuta")
        void erroreArchivio() {
            pagamentoAutorizzato(TRX);
            when(archivioRepository.save(any(ArchivioPagamento.class)))
                    .thenThrow(new IllegalStateException("db"));

            assertThatThrownBy(() -> service.eseguiPagamento(IUV, VERSATO))
                    .isInstanceOf(IllegalStateException.class);

            // in memoria la posizione è già PAGATA: in produzione la annulla il rollback della transazione
            assertThat(posizione.getStato()).isEqualTo("PAGATA");
            verifyNoInteractions(ricevutaMapper);
        }
    }
}