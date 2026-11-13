package it.pa.tributi.tributiservice.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final String SEGRETO = "chiave-di-test-lunga-almeno-32-caratteri!";
    private static final String ALTRO_SEGRETO = "un-altra-chiave-lunga-almeno-32-caratteri";
    private static final String CF = "RSSMRA80A01F839X";

    private final JwtService service = new JwtService(SEGRETO);


    @Nested
    @DisplayName("Token validi")
    class Validi {

        @Test
        @DisplayName("restituisce il codice fiscale contenuto nel subject")
        void codiceFiscale() {
            String token = Jwts.builder().subject(CF).signWith(chiave(SEGRETO)).compact();

            assertThat(service.estraiCodiceFiscale(token)).contains(CF);
        }

        @Test
        @DisplayName("token con scadenza futura: valido")
        void scadenzaFutura() {
            String token = Jwts.builder()
                    .subject(CF)
                    .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                    .signWith(chiave(SEGRETO))
                    .compact();

            assertThat(service.estraiCodiceFiscale(token)).contains(CF);
        }

        @Test
        @DisplayName("altri claim (per esempio il ruolo) non disturbano")
        void altriClaim() {
            String token = Jwts.builder()
                    .subject(CF)
                    .claim("ruolo", "OPERATORE")
                    .signWith(chiave(SEGRETO))
                    .compact();

            assertThat(service.estraiCodiceFiscale(token)).contains(CF);
        }

        @Test
        @DisplayName("token firmato e senza subject: vuoto")
        void senzaSubject() {
            String token = Jwts.builder().claim("ruolo", "CITTADINO").signWith(chiave(SEGRETO)).compact();

            assertThat(service.estraiCodiceFiscale(token)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Token non validi")
    class NonValidi {

        @Test
        @DisplayName("firmato con un'altra chiave: vuoto")
        void altraChiave() {
            String token = Jwts.builder().subject(CF).signWith(chiave(ALTRO_SEGRETO)).compact();

            assertThat(service.estraiCodiceFiscale(token)).isEmpty();
        }

        @Test
        @DisplayName("scaduto: vuoto")
        void scaduto() {
            String token = Jwts.builder()
                    .subject(CF)
                    .expiration(new Date(System.currentTimeMillis() - 60_000))
                    .signWith(chiave(SEGRETO))
                    .compact();

            assertThat(service.estraiCodiceFiscale(token)).isEmpty();
        }

        @Test
        @DisplayName("non ancora valido (notBefore nel futuro): vuoto")
        void nonAncoraValido() {
            String token = Jwts.builder()
                    .subject(CF)
                    .notBefore(new Date(System.currentTimeMillis() + 3_600_000))
                    .signWith(chiave(SEGRETO))
                    .compact();

            assertThat(service.estraiCodiceFiscale(token)).isEmpty();
        }

        @Test
        @DisplayName("senza firma: vuoto")
        void senzaFirma() {
            String token = Jwts.builder().subject(CF).compact();

            assertThat(service.estraiCodiceFiscale(token)).isEmpty();
        }

        @Test
        @DisplayName("firma alterata: vuoto")
        void firmaAlterata() {
            String token = Jwts.builder().subject(CF).signWith(chiave(SEGRETO)).compact();
            String alterato = token.substring(0, token.length() - 2)
                    + (token.endsWith("AA") ? "BB" : "AA");

            assertThat(service.estraiCodiceFiscale(alterato)).isEmpty();
        }

        @Test
        @DisplayName("contenuto modificato dopo la firma: vuoto")
        void contenutoModificato() {
            String vero = Jwts.builder().subject(CF).signWith(chiave(SEGRETO)).compact();
            String falso = Jwts.builder().subject("VRDLGI75B02H501Y").signWith(chiave(SEGRETO)).compact();
            String[] partiVere = vero.split("\\.");
            String[] partiFalse = falso.split("\\.");
            // contenuto del token falso con la firma di quello vero
            String manomesso = partiVere[0] + "." + partiFalse[1] + "." + partiVere[2];

            assertThat(service.estraiCodiceFiscale(manomesso)).isEmpty();
        }

        @ParameterizedTest(name = "token \"{0}\": vuoto")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "non-un-token", "a.b.c", "Bearer xyz"})
        void malformato(String token) {
            assertThat(service.estraiCodiceFiscale(token)).isEmpty();
        }
    }


    @Test
    @DisplayName("segreto più corto di 32 byte: errore alla creazione del servizio")
    void segretoTroppoCorto() {
        assertThatThrownBy(() -> new JwtService("troppo-corto"))
                .isInstanceOf(WeakKeyException.class);
    }

    @Test
    @DisplayName("segreto null: NullPointerException alla creazione del servizio")
    void segretoNull() {
        assertThatThrownBy(() -> new JwtService(null))
                .isInstanceOf(NullPointerException.class);
    }


    private static SecretKey chiave(String segreto) {
        return Keys.hmacShaKeyFor(segreto.getBytes(StandardCharsets.UTF_8));
    }
}