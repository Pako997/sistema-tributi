package it.pa.tributi.tributiservice.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import it.pa.tributi.tributiservice.repository.OperatoreRepository;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtFilter")
class JwtFilterTest {

    private static final String CF = "RSSMRA80A01F839X";

    @Mock private JwtService jwtService;
    @Mock private OperatoreRepository operatoreRepository;

    private JwtFilter filtro;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void prepara() {
        SecurityContextHolder.clearContext();
        filtro = new JwtFilter(jwtService, operatoreRepository);
        request = new MockHttpServletRequest("GET", "/api/posizioni");
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @AfterEach
    void pulisci() {
        SecurityContextHolder.clearContext();
    }


    @Nested
    @DisplayName("Token valido")
    class TokenValido {

        @Test
        @DisplayName("codice fiscale di un operatore: autenticato con ROLE_OPERATORE")
        void operatore() throws Exception {
            request.addHeader("Authorization", "Bearer token-valido");
            when(jwtService.estraiCodiceFiscale("token-valido")).thenReturn(Optional.of(CF));
            when(operatoreRepository.existsById(CF)).thenReturn(true);

            filtro.doFilter(request, response, chain);

            assertThat(ruoli(autenticazione())).containsExactly("ROLE_OPERATORE");
        }

        @Test
        @DisplayName("codice fiscale non presente tra gli operatori: autenticato con ROLE_CITTADINO")
        void cittadino() throws Exception {
            request.addHeader("Authorization", "Bearer token-valido");
            when(jwtService.estraiCodiceFiscale("token-valido")).thenReturn(Optional.of(CF));
            when(operatoreRepository.existsById(CF)).thenReturn(false);

            filtro.doFilter(request, response, chain);

            assertThat(ruoli(autenticazione())).containsExactly("ROLE_CITTADINO");
        }

        @Test
        @DisplayName("l'utente autenticato ha come nome il codice fiscale, nessuna credenziale e risulta autenticato")
        void datiAutenticazione() throws Exception {
            request.addHeader("Authorization", "Bearer token-valido");
            when(jwtService.estraiCodiceFiscale("token-valido")).thenReturn(Optional.of(CF));
            when(operatoreRepository.existsById(CF)).thenReturn(false);

            filtro.doFilter(request, response, chain);

            Authentication auth = autenticazione();
            assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
            assertThat(auth.getName()).isEqualTo(CF);
            assertThat(auth.getPrincipal()).isEqualTo(CF);
            assertThat(auth.getCredentials()).isNull();
            assertThat(auth.isAuthenticated()).isTrue();
        }

        @Test
        @DisplayName("al JwtService passa il token senza il prefisso Bearer")
        void tokenSenzaPrefisso() throws Exception {
            request.addHeader("Authorization", "Bearer abc.def.ghi");
            when(jwtService.estraiCodiceFiscale("abc.def.ghi")).thenReturn(Optional.empty());

            filtro.doFilter(request, response, chain);

            verify(jwtService).estraiCodiceFiscale("abc.def.ghi");
        }

        @Test
        @DisplayName("la richiesta prosegue lungo la catena")
        void catenaProsegue() throws Exception {
            request.addHeader("Authorization", "Bearer token-valido");
            when(jwtService.estraiCodiceFiscale("token-valido")).thenReturn(Optional.of(CF));
            when(operatoreRepository.existsById(CF)).thenReturn(false);

            filtro.doFilter(request, response, chain);

            assertThat(chain.getRequest()).isSameAs(request);
        }
    }


    @Nested
    @DisplayName("Token assente o non valido")
    class TokenNonValido {

        @Test
        @DisplayName("nessun header Authorization: nessuna autenticazione, la richiesta prosegue")
        void senzaHeader() throws Exception {
            filtro.doFilter(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            assertThat(chain.getRequest()).isSameAs(request);
            verifyNoInteractions(jwtService, operatoreRepository);
        }

        @ParameterizedTest(name = "header \"{0}\": ignorato")
        @ValueSource(strings = {"Basic dXRlbnRlOnBhc3N3b3Jk", "bearer token-valido", "Bearertoken-valido", "token-valido"})
        void headerNonBearer(String header) throws Exception {
            request.addHeader("Authorization", header);

            filtro.doFilter(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            assertThat(chain.getRequest()).isSameAs(request);
            verifyNoInteractions(jwtService, operatoreRepository);
        }

        @Test
        @DisplayName("token rifiutato dal JwtService: nessuna autenticazione, nessuna ricerca dell'operatore")
        void tokenRifiutato() throws Exception {
            request.addHeader("Authorization", "Bearer token-scaduto");
            when(jwtService.estraiCodiceFiscale("token-scaduto")).thenReturn(Optional.empty());

            filtro.doFilter(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            assertThat(chain.getRequest()).isSameAs(request);
            verifyNoInteractions(operatoreRepository);
        }

        @Test
        @DisplayName("solo \"Bearer \": al JwtService arriva una stringa vuota")
        void bearerVuoto() throws Exception {
            request.addHeader("Authorization", "Bearer ");
            when(jwtService.estraiCodiceFiscale("")).thenReturn(Optional.empty());

            filtro.doFilter(request, response, chain);

            verify(jwtService).estraiCodiceFiscale("");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("token non valido: un'autenticazione già presente nel contesto non viene rimossa")
        void autenticazioneEsistente() throws Exception {
            UsernamePasswordAuthenticationToken esistente =
                    new UsernamePasswordAuthenticationToken("ALTRO", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(esistente);
            request.addHeader("Authorization", "Bearer token-scaduto");
            when(jwtService.estraiCodiceFiscale("token-scaduto")).thenReturn(Optional.empty());

            filtro.doFilter(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(esistente);
        }
    }

    @Test
    @DisplayName("errore del repository operatori: l'eccezione esce e la richiesta non prosegue")
    void erroreRepository() {
        FilterChain catena = mock(FilterChain.class);
        request.addHeader("Authorization", "Bearer token-valido");
        when(jwtService.estraiCodiceFiscale("token-valido")).thenReturn(Optional.of(CF));
        when(operatoreRepository.existsById(CF)).thenThrow(new IllegalStateException("db non raggiungibile"));

        assertThatThrownBy(() -> filtro.doFilter(request, response, catena))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(catena);
    }


    private static Authentication autenticazione() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        return auth;
    }

    private static List<String> ruoli(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }
}