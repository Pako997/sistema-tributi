package it.pa.tributi.tributiservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import it.pa.tributi.tributiservice.dto.PosizioneDTO;
import it.pa.tributi.tributiservice.entity.Ente;
import it.pa.tributi.tributiservice.entity.Posizione;

@DisplayName("PosizioneMapper")
class PosizioneMapperTest {

    private static final LocalDate EMISSIONE = LocalDate.of(2026, 1, 10);
    private static final LocalDate SCADENZA = LocalDate.of(2026, 3, 10);
    private static final LocalDate PAGAMENTO = LocalDate.of(2026, 2, 20);

    private final PosizioneMapper mapper = new PosizioneMapper();

    @Nested
    @DisplayName("toDTO(Posizione)")
    class Singola {

        @Test
        @DisplayName("copia tutti i campi della posizione e dell'ente")
        void tuttiICampi() {
            PosizioneDTO dto = mapper.toDTO(posizione("IUV-1"));

            assertThat(dto.getIuv()).isEqualTo("IUV-1");
            assertThat(dto.getCodiceFiscale()).isEqualTo("RSSMRA80A01F839X");
            assertThat(dto.getTipoTributo()).isEqualTo("TARI");
            assertThat(dto.getImporto()).isEqualByComparingTo("130.00");
            assertThat(dto.getImportoOriginale()).isEqualByComparingTo("100.00");
            assertThat(dto.getStato()).isEqualTo("PAGATA");
            assertThat(dto.getDataEmissione()).isEqualTo(EMISSIONE);
            assertThat(dto.getDataScadenza()).isEqualTo(SCADENZA);
            assertThat(dto.getDataPagamento()).isEqualTo(PAGAMENTO);
            assertThat(dto.getCodiceEnte()).isEqualTo("C_F839");
            assertThat(dto.getEnteEmittente()).isEqualTo("Comune di Napoli");
            assertThat(dto.getContattoEnte()).isEqualTo("tributi@comune.napoli.it");
        }

        @Test
        @DisplayName("campi facoltativi null (data di pagamento, contatto ente): restano null")
        void campiFacoltativiNull() {
            Posizione p = posizione("IUV-1");
            p.setDataPagamento(null);
            p.getEnte().setContatto(null);

            PosizioneDTO dto = mapper.toDTO(p);

            assertThat(dto.getDataPagamento()).isNull();
            assertThat(dto.getContattoEnte()).isNull();
        }

        @Test
        @DisplayName("ogni chiamata crea un DTO nuovo")
        void dtoNuovo() {
            Posizione p = posizione("IUV-1");

            assertThat(mapper.toDTO(p)).isNotSameAs(mapper.toDTO(p));
        }

        @Test
        @DisplayName("posizione senza ente: NullPointerException")
        void senzaEnte() {
            Posizione p = posizione("IUV-1");
            p.setEnte(null);

            assertThatThrownBy(() -> mapper.toDTO(p)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("posizione null: NullPointerException")
        void posizioneNull() {
            Posizione nessuna = null;

            assertThatThrownBy(() -> mapper.toDTO(nessuna)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("toDTO(List)")
    class Lista {

        @Test
        @DisplayName("converte ogni posizione mantenendo l'ordine")
        void ordineMantenuto() {
            List<PosizioneDTO> dto = mapper.toDTO(List.of(posizione("IUV-1"), posizione("IUV-2"), posizione("IUV-3")));

            assertThat(dto).extracting(PosizioneDTO::getIuv).containsExactly("IUV-1", "IUV-2", "IUV-3");
        }

        @Test
        @DisplayName("lista vuota: lista vuota")
        void listaVuota() {
            assertThat(mapper.toDTO(List.<Posizione>of())).isEmpty();
        }

        @Test
        @DisplayName("la lista restituita non è modificabile")
        void nonModificabile() {
            List<PosizioneDTO> dto = mapper.toDTO(List.of(posizione("IUV-1")));
            PosizioneDTO altro = new PosizioneDTO();

            assertThatThrownBy(() -> dto.add(altro)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("una posizione senza ente fa fallire l'intera conversione")
        void unaSenzaEnte() {
            Posizione orfana = posizione("ORFANA");
            orfana.setEnte(null);
            List<Posizione> posizioni = new ArrayList<>(List.of(posizione("IUV-1"), orfana));

            assertThatThrownBy(() -> mapper.toDTO(posizioni)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lista null: NullPointerException")
        void listaNull() {
            List<Posizione> nessuna = null;

            assertThatThrownBy(() -> mapper.toDTO(nessuna)).isInstanceOf(NullPointerException.class);
        }
    }


    private static Posizione posizione(String iuv) {
        Ente ente = new Ente();
        ente.setCodiceEnte("C_F839");
        ente.setDenominazione("Comune di Napoli");
        ente.setContatto("tributi@comune.napoli.it");

        Posizione p = new Posizione();
        p.setIuv(iuv);
        p.setCodiceFiscale("RSSMRA80A01F839X");
        p.setTipoTributo("TARI");
        p.setImporto(new BigDecimal("130.00"));
        p.setImportoOriginale(new BigDecimal("100.00"));
        p.setStato("PAGATA");
        p.setDataEmissione(EMISSIONE);
        p.setDataScadenza(SCADENZA);
        p.setDataPagamento(PAGAMENTO);
        p.setEnte(ente);
        return p;
    }
}