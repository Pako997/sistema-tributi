package it.pa.tributi.tributiservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import it.pa.tributi.tributiservice.dto.DebitoRequest;
import it.pa.tributi.tributiservice.dto.PagamentoRequest;
import it.pa.tributi.tributiservice.dto.PosizioneDTO;
import it.pa.tributi.tributiservice.dto.RicevutaDTO;
import it.pa.tributi.tributiservice.exception.ErroreTributi;
import it.pa.tributi.tributiservice.exception.TributiException;
import it.pa.tributi.tributiservice.service.PagamentoService;
import it.pa.tributi.tributiservice.service.PosizioneService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TributiController")
class TributiControllerTest {

    private static final String CF_UTENTE = "RSSMRA80A01F839X";
    private static final String CF_RICHIESTO = "VRDLGI75B02H501Y";

    @Mock private PosizioneService posizioneService;
    @Mock private PagamentoService pagamentoService;

    private TributiController controller;

    @BeforeEach
    void crea() {
        controller = new TributiController(posizioneService, pagamentoService);
    }


    @Nested
    @DisplayName("eseguiPagamento")
    class EseguiPagamento {

        private PagamentoRequest richiesta;

        @BeforeEach
        void prepara() {
            richiesta = mock(PagamentoRequest.class);
            when(richiesta.iuv()).thenReturn("IUV-1");
        }

        @Test
        @DisplayName("segna la posizione IN_CORSO, poi paga e restituisce la ricevuta")
        void pagamento() {
            BigDecimal versato = new BigDecimal("100.00");
            RicevutaDTO ricevuta = mock(RicevutaDTO.class);
            when(richiesta.importoVersato()).thenReturn(versato);
            when(pagamentoService.eseguiPagamento("IUV-1", versato)).thenReturn(ricevuta);

            assertThat(controller.eseguiPagamento(richiesta)).isSameAs(ricevuta);

            InOrder ordine = inOrder(posizioneService, pagamentoService);
            ordine.verify(posizioneService).segnaInCorso("IUV-1");
            ordine.verify(pagamentoService).eseguiPagamento("IUV-1", versato);
        }

        @Test
        @DisplayName("IUV inesistente: l'eccezione esce, nessun pagamento")
        void iuvNonTrovato() {
            doThrow(new TributiException(ErroreTributi.IUV_NON_TROVATO))
                    .when(posizioneService).segnaInCorso("IUV-1");

            assertThatThrownBy(() -> controller.eseguiPagamento(richiesta))
                    .isInstanceOf(TributiException.class);

            verifyNoInteractions(pagamentoService);
        }

        @Test
        @DisplayName("pagamento fallito: l'eccezione esce, la posizione è già stata segnata IN_CORSO")
        void pagamentoFallito() {
            when(richiesta.importoVersato()).thenReturn(new BigDecimal("100.00"));
            when(pagamentoService.eseguiPagamento(any(), any()))
                    .thenThrow(new TributiException(ErroreTributi.PAGAMENTO_NON_RIUSCITO));

            assertThatThrownBy(() -> controller.eseguiPagamento(richiesta))
                    .isInstanceOf(TributiException.class);

            verify(posizioneService).segnaInCorso("IUV-1");
        }
    }


    @Nested
    @DisplayName("verificaPosizione")
    class VerificaPosizione {

        @Test
        @DisplayName("operatore: cerca il codice fiscale richiesto")
        void operatore() {
            Authentication auth = utente(CF_UTENTE, "ROLE_OPERATORE");
            List<PosizioneDTO> posizioni = List.of(mock(PosizioneDTO.class));
            when(posizioneService.verificaPosizione(CF_RICHIESTO)).thenReturn(posizioni);

            assertThat(controller.verificaPosizione(CF_RICHIESTO, auth)).isSameAs(posizioni);
        }

        @Test
        @DisplayName("operatore senza codice fiscale: passa null al service (che risponde PARAMETRI_MANCANTI)")
        void operatoreSenzaCodiceFiscale() {
            Authentication auth = utente(CF_UTENTE, "ROLE_OPERATORE");

            controller.verificaPosizione(null, auth);

            verify(posizioneService).verificaPosizione(null);
        }

        @Test
        @DisplayName("operatore con più ruoli: riconosciuto")
        void operatorePiuRuoli() {
            Authentication auth = utente(CF_UTENTE, "ROLE_CITTADINO", "ROLE_OPERATORE");

            controller.verificaPosizione(CF_RICHIESTO, auth);

            verify(posizioneService).verificaPosizione(CF_RICHIESTO);
        }

        @Test
        @DisplayName("cittadino: cerca sempre il proprio codice fiscale, anche se ne chiede un altro")
        void cittadinoAltroCodice() {
            Authentication auth = utente(CF_UTENTE, "ROLE_CITTADINO");

            controller.verificaPosizione(CF_RICHIESTO, auth);

            verify(posizioneService).verificaPosizione(CF_UTENTE);
        }

        @Test
        @DisplayName("cittadino senza parametro: cerca il proprio codice fiscale")
        void cittadinoSenzaParametro() {
            Authentication auth = utente(CF_UTENTE, "ROLE_CITTADINO");

            controller.verificaPosizione(null, auth);

            verify(posizioneService).verificaPosizione(CF_UTENTE);
        }

        @Test
        @DisplayName("utente senza ruoli: trattato come cittadino")
        void senzaRuoli() {
            Authentication auth = utente(CF_UTENTE);

            controller.verificaPosizione(CF_RICHIESTO, auth);

            verify(posizioneService).verificaPosizione(CF_UTENTE);
        }

        @Test
        @DisplayName("ruolo OPERATORE senza prefisso ROLE_: trattato come cittadino")
        void ruoloSenzaPrefisso() {
            Authentication auth = utente(CF_UTENTE, "OPERATORE");

            controller.verificaPosizione(CF_RICHIESTO, auth);

            verify(posizioneService).verificaPosizione(CF_UTENTE);
        }
    }


    @Nested
    @DisplayName("registraDebito")
    class RegistraDebito {

        @Test
        @DisplayName("restituisce una mappa con il solo IUV creato")
        void restituisceIuv() {
            DebitoRequest richiesta = mock(DebitoRequest.class);
            when(posizioneService.creaPosizione(richiesta)).thenReturn("17000000000001234");

            Map<String, String> risposta = controller.registraDebito(richiesta);

            assertThat(risposta).containsExactly(Map.entry("iuv", "17000000000001234"));
        }

        @Test
        @DisplayName("errore del service: l'eccezione esce")
        void erroreService() {
            DebitoRequest richiesta = mock(DebitoRequest.class);
            when(posizioneService.creaPosizione(richiesta))
                    .thenThrow(new TributiException(ErroreTributi.ENTE_NON_TROVATO));

            assertThatThrownBy(() -> controller.registraDebito(richiesta))
                    .isInstanceOf(TributiException.class);
        }

        @Test
        @DisplayName("l'espressione di @PreAuthorize è scritta correttamente")
        void espressionePreAuthorize() throws NoSuchMethodException {
            String espressione = TributiController.class
                    .getMethod("registraDebito", DebitoRequest.class)
                    .getAnnotation(PreAuthorize.class)
                    .value();
            SpelExpressionParser parser = new SpelExpressionParser();

            assertThatCode(() -> parser.parseExpression(espressione)).doesNotThrowAnyException();
        }
    }

    private static Authentication utente(String codiceFiscale, String... ruoli) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(codiceFiscale);
        List<SimpleGrantedAuthority> autorita = java.util.Arrays.stream(ruoli)
                .map(SimpleGrantedAuthority::new)
                .toList();
        doReturn(autorita).when(auth).getAuthorities();
        return auth;
    }
}