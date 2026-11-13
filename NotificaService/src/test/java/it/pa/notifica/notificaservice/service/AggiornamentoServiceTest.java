package it.pa.notifica.notificaservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.pa.notifica.notificaservice.entity.Posizione;
import it.pa.notifica.notificaservice.repository.PosizioneRepository;
import it.pa.notifica.notificaservice.strategy.StrategiaRivalutazione;

@ExtendWith(MockitoExtension.class)
@DisplayName("AggiornamentoService")
class AggiornamentoServiceTest {

    @Mock
    private PosizioneRepository posizioneRepository;

    @Mock
    private RegistroStrategie registro;

    @Mock
    private StrategiaRivalutazione strategiaMulta;

    @Mock
    private StrategiaRivalutazione strategiaTari;

    private AggiornamentoService service;

    @BeforeEach
    void crea() {
        service = new AggiornamentoService(posizioneRepository, registro);
    }

    @Test
    @DisplayName("cerca le posizioni scadute rispetto alla data di oggi")
    void cercaConDataDiOggi() {
        when(posizioneRepository.findScadute(LocalDate.now())).thenReturn(List.of());

        service.rivalutaScadute();

        verify(posizioneRepository).findScadute(LocalDate.now());
    }

    @Test
    @DisplayName("nessuna posizione scaduta: il registro non viene interrogato")
    void nessunaScaduta() {
        when(posizioneRepository.findScadute(any())).thenReturn(List.of());

        service.rivalutaScadute();

        verifyNoInteractions(registro);
    }

    @Test
    @DisplayName("nuovo importo diverso: la posizione viene aggiornata")
    void importoAggiornato() {
        Posizione p = posizione("IUV-1", "MULTA", "100.00", 70);
        when(posizioneRepository.findScadute(any())).thenReturn(List.of(p));
        when(registro.per("MULTA")).thenReturn(Optional.of(strategiaMulta));
        when(strategiaMulta.calcolaImporto(p, 70L)).thenReturn(new BigDecimal("200.00"));

        service.rivalutaScadute();

        assertThat(p.getImporto()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("alla strategia passa i giorni di ritardo esatti rispetto alla scadenza")
    void giorniDiRitardo() {
        Posizione p = posizione("IUV-1", "TARI", "100.00", 15);
        when(posizioneRepository.findScadute(any())).thenReturn(List.of(p));
        when(registro.per("TARI")).thenReturn(Optional.of(strategiaTari));
        when(strategiaTari.calcolaImporto(p, 15L)).thenReturn(new BigDecimal("101.32"));

        service.rivalutaScadute();

        verify(strategiaTari).calcolaImporto(p, 15L);
    }

    @Test
    @DisplayName("stesso importo con scala diversa (100.0 e 100.00): la posizione non viene toccata")
    void importoInvariato() {
        BigDecimal originale = new BigDecimal("100.00");
        Posizione p = posizione("IUV-1", "MULTA", "100.00", 70);
        p.setImporto(originale);
        when(posizioneRepository.findScadute(any())).thenReturn(List.of(p));
        when(registro.per("MULTA")).thenReturn(Optional.of(strategiaMulta));
        when(strategiaMulta.calcolaImporto(p, 70L)).thenReturn(new BigDecimal("100.0"));

        service.rivalutaScadute();

        assertThat(p.getImporto()).isSameAs(originale);
    }

    @Test
    @DisplayName("nessuna strategia per il tributo: importo invariato, nessun calcolo")
    void tributoSenzaStrategia() {
        BigDecimal originale = new BigDecimal("100.00");
        Posizione p = posizione("IUV-1", "IMU", "100.00", 70);
        p.setImporto(originale);
        when(posizioneRepository.findScadute(any())).thenReturn(List.of(p));
        when(registro.per("IMU")).thenReturn(Optional.empty());

        service.rivalutaScadute();

        assertThat(p.getImporto()).isSameAs(originale);
    }

    @Test
    @DisplayName("più posizioni: ognuna con la strategia del proprio tributo")
    void piuPosizioni() {
        Posizione multa = posizione("IUV-M", "MULTA", "100.00", 70);
        Posizione tari = posizione("IUV-T", "TARI", "100.00", 10);
        when(posizioneRepository.findScadute(any())).thenReturn(List.of(multa, tari));
        when(registro.per("MULTA")).thenReturn(Optional.of(strategiaMulta));
        when(registro.per("TARI")).thenReturn(Optional.of(strategiaTari));
        when(strategiaMulta.calcolaImporto(multa, 70L)).thenReturn(new BigDecimal("200.00"));
        when(strategiaTari.calcolaImporto(tari, 10L)).thenReturn(new BigDecimal("100.87"));

        service.rivalutaScadute();

        assertThat(multa.getImporto()).isEqualByComparingTo("200.00");
        assertThat(tari.getImporto()).isEqualByComparingTo("100.87");
        verify(strategiaMulta, never()).calcolaImporto(tari, 10L);
        verify(strategiaTari, never()).calcolaImporto(multa, 70L);
    }

    @Test
    @DisplayName("non chiama save: il salvataggio si affida al dirty checking della transazione")
    void nessunSave() {
        Posizione p = posizione("IUV-1", "MULTA", "100.00", 70);
        when(posizioneRepository.findScadute(any())).thenReturn(List.of(p));
        when(registro.per("MULTA")).thenReturn(Optional.of(strategiaMulta));
        when(strategiaMulta.calcolaImporto(p, 70L)).thenReturn(new BigDecimal("200.00"));

        service.rivalutaScadute();

        verify(posizioneRepository, never()).save(any());
        verify(posizioneRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("strategia che restituisce null: NullPointerException")
    void strategiaRestituisceNull() {
        Posizione p = posizione("IUV-1", "MULTA", "100.00", 70);
        when(posizioneRepository.findScadute(any())).thenReturn(List.of(p));
        when(registro.per("MULTA")).thenReturn(Optional.of(strategiaMulta));
        when(strategiaMulta.calcolaImporto(p, 70L)).thenReturn(null);

        assertThatThrownBy(() -> service.rivalutaScadute())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("errore su una posizione: le successive non vengono rivalutate")
    void erroreInterrompe() {
        Posizione prima = posizione("IUV-1", "MULTA", "100.00", 70);
        Posizione seconda = posizione("IUV-2", "TARI", "100.00", 10);
        when(posizioneRepository.findScadute(any())).thenReturn(List.of(prima, seconda));
        when(registro.per("MULTA")).thenReturn(Optional.of(strategiaMulta));
        when(strategiaMulta.calcolaImporto(prima, 70L)).thenThrow(new IllegalStateException("errore"));

        assertThatThrownBy(() -> service.rivalutaScadute())
                .isInstanceOf(IllegalStateException.class);

        verify(registro, never()).per("TARI");
        verify(strategiaTari, never()).calcolaImporto(any(), anyLong());
    }


    private static Posizione posizione(String iuv, String tributo, String importo, long giorniRitardo) {
        Posizione p = new Posizione();
        p.setIuv(iuv);
        p.setTipoTributo(tributo);
        p.setImporto(new BigDecimal(importo));
        p.setImportoOriginale(new BigDecimal(importo));
        p.setStato("DA_PAGARE");
        p.setDataScadenza(LocalDate.now().minusDays(giorniRitardo));
        return p;
    }
}