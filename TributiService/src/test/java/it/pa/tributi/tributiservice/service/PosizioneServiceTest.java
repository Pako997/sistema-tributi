package it.pa.tributi.tributiservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.pa.tributi.tributiservice.dto.DebitoRequest;
import it.pa.tributi.tributiservice.dto.PosizioneDTO;
import it.pa.tributi.tributiservice.entity.Ente;
import it.pa.tributi.tributiservice.entity.Posizione;
import it.pa.tributi.tributiservice.exception.ErroreTributi;
import it.pa.tributi.tributiservice.exception.TributiException;
import it.pa.tributi.tributiservice.mapper.PosizioneMapper;
import it.pa.tributi.tributiservice.repository.EnteRepository;
import it.pa.tributi.tributiservice.repository.PosizioneRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PosizioneService")
class PosizioneServiceTest {

    private static final int GIORNI_MINIMI = 30;
    private static final String CF = "RSSMRA80A01F839X";
    private static final String IUV_GENERATO = "17000000000001234";

    @Mock private PosizioneRepository posizioneRepository;
    @Mock private PosizioneMapper posizioneMapper;
    @Mock private EnteRepository enteRepository;
    @Mock private GeneratoreIuv generatoreIuv;

    private PosizioneService service;

    @BeforeEach
    void crea() {
        service = new PosizioneService(posizioneRepository, posizioneMapper, enteRepository,
                generatoreIuv, GIORNI_MINIMI);
    }

    @Nested
    @DisplayName("segnaInCorso")
    class SegnaInCorso {

        @Test
        @DisplayName("la posizione diventa IN_CORSO")
        void inCorso() {
            Posizione p = posizione("DA_PAGARE");
            when(posizioneRepository.findByIuv("IUV-1")).thenReturn(Optional.of(p));

            service.segnaInCorso("IUV-1");

            assertThat(p.getStato()).isEqualTo("IN_CORSO");
        }

        @Test
        @DisplayName("non chiama save: il salvataggio si affida alla transazione")
        void nessunSave() {
            when(posizioneRepository.findByIuv("IUV-1")).thenReturn(Optional.of(posizione("DA_PAGARE")));

            service.segnaInCorso("IUV-1");

            verify(posizioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("posizione già PAGATA: torna IN_CORSO (lo stato di partenza non viene controllato)")
        void giaPagata() {
            Posizione p = posizione("PAGATA");
            when(posizioneRepository.findByIuv("IUV-1")).thenReturn(Optional.of(p));

            service.segnaInCorso("IUV-1");

            assertThat(p.getStato()).isEqualTo("IN_CORSO");
        }

        @Test
        @DisplayName("IUV inesistente: IUV_NON_TROVATO")
        void iuvNonTrovato() {
            when(posizioneRepository.findByIuv("NON_ESISTE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.segnaInCorso("NON_ESISTE"))
                    .isInstanceOf(TributiException.class)
                    .extracting("errore").isEqualTo(ErroreTributi.IUV_NON_TROVATO);
        }
    }


    @Nested
    @DisplayName("verificaPosizione")
    class VerificaPosizione {

        @Test
        @DisplayName("restituisce le posizioni del codice fiscale convertite dal mapper")
        void restituisceDto() {
            List<Posizione> posizioni = List.of(posizione("DA_PAGARE"), posizione("PAGATA"));
            List<PosizioneDTO> dto = List.of(mock(PosizioneDTO.class), mock(PosizioneDTO.class));
            when(posizioneRepository.findByCodiceFiscale(CF)).thenReturn(posizioni);
            when(posizioneMapper.toDTO(posizioni)).thenReturn(dto);

            assertThat(service.verificaPosizione(CF)).isSameAs(dto);
        }

        @Test
        @DisplayName("nessuna posizione: restituisce quello che il mapper produce dalla lista vuota")
        void nessunaPosizione() {
            List<Posizione> vuota = List.of();
            when(posizioneRepository.findByCodiceFiscale(CF)).thenReturn(vuota);
            when(posizioneMapper.toDTO(vuota)).thenReturn(List.of());

            assertThat(service.verificaPosizione(CF)).isEmpty();
        }

        @ParameterizedTest(name = "codice fiscale \"{0}\": PARAMETRI_MANCANTI")
        @NullSource
        @ValueSource(strings = {"", "   "})
        void codiceFiscaleMancante(String cf) {
            assertThatThrownBy(() -> service.verificaPosizione(cf))
                    .isInstanceOf(TributiException.class)
                    .extracting("errore").isEqualTo(ErroreTributi.PARAMETRI_MANCANTI);

            verifyNoInteractions(posizioneRepository, posizioneMapper);
        }
    }


    @Nested
    @DisplayName("creaPosizione")
    class CreaPosizione {

        private Ente ente;

        @BeforeEach
        void prepara() {
            ente = new Ente();
            ente.setCodiceEnte("C_F839");
            lenient().when(posizioneRepository.save(any(Posizione.class))).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(generatoreIuv.genera()).thenReturn(IUV_GENERATO);
            lenient().when(enteRepository.findById("C_F839")).thenReturn(Optional.of(ente));
        }

        @Test
        @DisplayName("restituisce lo IUV generato")
        void restituisceIuv() {
            assertThat(service.creaPosizione(richiesta(LocalDate.now().plusDays(40), "C_F839")))
                    .isEqualTo(IUV_GENERATO);
        }

        @Test
        @DisplayName("salva una posizione con i dati della richiesta, lo IUV generato e l'ente trovato")
        void posizioneSalvata() {
            LocalDate scadenza = LocalDate.now().plusDays(40);

            service.creaPosizione(richiesta(scadenza, "C_F839"));

            ArgumentCaptor<Posizione> captor = ArgumentCaptor.forClass(Posizione.class);
            verify(posizioneRepository).save(captor.capture());
            Posizione salvata = captor.getValue();
            assertThat(salvata.getIuv()).isEqualTo(IUV_GENERATO);
            assertThat(salvata.getCodiceFiscale()).isEqualTo(CF);
            assertThat(salvata.getTipoTributo()).isEqualTo("MULTA");
            assertThat(salvata.getImporto()).isEqualByComparingTo("80.00");
            assertThat(salvata.getDataScadenza()).isEqualTo(scadenza);
            assertThat(salvata.getEnte()).isSameAs(ente);
        }

        @Test
        @DisplayName("restituisce lo IUV della posizione salvata, non quello generato")
        void iuvDellaPosizioneSalvata() {
            Posizione salvataDalDb = posizione("DA_PAGARE");
            salvataDalDb.setIuv("IUV-DAL-DB");
            when(posizioneRepository.save(any(Posizione.class))).thenReturn(salvataDalDb);

            assertThat(service.creaPosizione(richiesta(LocalDate.now().plusDays(40), "C_F839")))
                    .isEqualTo("IUV-DAL-DB");
        }

        @Test
        @DisplayName("scadenza esattamente a 30 giorni: accettata")
        void scadenzaAlLimite() {
            assertThat(service.creaPosizione(richiesta(LocalDate.now().plusDays(GIORNI_MINIMI), "C_F839")))
                    .isEqualTo(IUV_GENERATO);
        }

        @ParameterizedTest(name = "scadenza fra {0} giorni: SCADENZA_NON_VALIDA, nulla viene creato")
        @ValueSource(ints = {-1, 0, 29})
        void scadenzaTroppoVicina(int giorni) {
            DebitoRequest r = richiesta(LocalDate.now().plusDays(giorni), "C_F839");

            assertThatThrownBy(() -> service.creaPosizione(r))
                    .isInstanceOf(TributiException.class)
                    .extracting("errore").isEqualTo(ErroreTributi.SCADENZA_NON_VALIDA);

            verifyNoInteractions(enteRepository, generatoreIuv);
            verify(posizioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("giorni minimi configurabili")
        void giorniConfigurabili() {
            PosizioneService conDieci = new PosizioneService(posizioneRepository, posizioneMapper,
                    enteRepository, generatoreIuv, 10);

            assertThat(conDieci.creaPosizione(richiesta(LocalDate.now().plusDays(10), "C_F839")))
                    .isEqualTo(IUV_GENERATO);
        }

        @Test
        @DisplayName("ente inesistente: ENTE_NON_TROVATO, nessuno IUV generato e nessun salvataggio")
        void enteNonTrovato() {
            when(enteRepository.findById("NON_ESISTE")).thenReturn(Optional.empty());
            DebitoRequest r = richiesta(LocalDate.now().plusDays(40), "NON_ESISTE");

            assertThatThrownBy(() -> service.creaPosizione(r))
                    .isInstanceOf(TributiException.class)
                    .extracting("errore").isEqualTo(ErroreTributi.ENTE_NON_TROVATO);

            verifyNoInteractions(generatoreIuv);
            verify(posizioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("importo zero: accettato (il service non controlla l'importo)")
        void importoZero() {
            DebitoRequest r = richiesta(LocalDate.now().plusDays(40), "C_F839");
            when(r.importo()).thenReturn(BigDecimal.ZERO);

            assertThat(service.creaPosizione(r)).isEqualTo(IUV_GENERATO);
        }

        @Test
        @DisplayName("data di scadenza null: NullPointerException")
        void scadenzaNull() {
            DebitoRequest r = richiesta(null, "C_F839");

            assertThatThrownBy(() -> service.creaPosizione(r))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private static DebitoRequest richiesta(LocalDate scadenza, String codiceEnte) {
        DebitoRequest r = mock(DebitoRequest.class);
        lenient().when(r.dataScadenza()).thenReturn(scadenza);
        lenient().when(r.codiceEnte()).thenReturn(codiceEnte);
        lenient().when(r.codiceFiscale()).thenReturn(CF);
        lenient().when(r.tipoTributo()).thenReturn("MULTA");
        lenient().when(r.importo()).thenReturn(new BigDecimal("80.00"));
        return r;
    }

    private static Posizione posizione(String stato) {
        Posizione p = new Posizione();
        p.setIuv("IUV-1");
        p.setCodiceFiscale(CF);
        p.setStato(stato);
        return p;
    }
}