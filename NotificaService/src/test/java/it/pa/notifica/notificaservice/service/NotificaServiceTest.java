package it.pa.notifica.notificaservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.pa.notifica.notificaservice.entity.Notifica;
import it.pa.notifica.notificaservice.entity.Posizione;
import it.pa.notifica.notificaservice.enums.TipoNotifica;
import it.pa.notifica.notificaservice.external.SistemaPostale;
import it.pa.notifica.notificaservice.repository.NotificaRepository;
import it.pa.notifica.notificaservice.repository.PosizioneRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificaService")
class NotificaServiceTest {

    private static final int BREVE = 15;
    private static final int LUNGO = 30;
    private static final String TIPO_BREVE = TipoNotifica.SOLLECITO_BREVE.name();
    private static final String TIPO_LUNGO = TipoNotifica.SOLLECITO_LUNGO.name();

    @Mock
    private PosizioneRepository posizioneRepository;

    @Mock
    private NotificaRepository notificaRepository;

    @Mock
    private SistemaPostale sistemaPostale;

    private NotificaService service;
    private LocalDate oggi;

    @BeforeEach
    void crea() {
        service = new NotificaService(posizioneRepository, notificaRepository, sistemaPostale, BREVE, LUNGO);
        oggi = LocalDate.now();
        // di default nessuna posizione da notificare; i singoli test sovrascrivono i casi che servono
        lenient().when(posizioneRepository.findDaNotificare(any(), any(), any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("interroga prima il sollecito breve (oggi, oggi+15], poi il lungo (oggi+15, oggi+30]")
    void intervalli() {
        service.inviaNotifiche();

        InOrder ordine = inOrder(posizioneRepository);
        ordine.verify(posizioneRepository).findDaNotificare(oggi, oggi.plusDays(BREVE), TIPO_BREVE);
        ordine.verify(posizioneRepository).findDaNotificare(oggi.plusDays(BREVE), oggi.plusDays(LUNGO), TIPO_LUNGO);
    }

    @Test
    @DisplayName("gli intervalli seguono i giorni configurati")
    void giorniConfigurati() {
        NotificaService altro = new NotificaService(posizioneRepository, notificaRepository, sistemaPostale, 7, 20);

        altro.inviaNotifiche();

        verify(posizioneRepository).findDaNotificare(oggi, oggi.plusDays(7), TIPO_BREVE);
        verify(posizioneRepository).findDaNotificare(oggi.plusDays(7), oggi.plusDays(20), TIPO_LUNGO);
    }


    @Test
    @DisplayName("nessuna posizione: nessun invio e nessuna notifica registrata")
    void nessunaPosizione() {
        service.inviaNotifiche();

        verifyNoInteractions(sistemaPostale, notificaRepository);
    }

    @Test
    @DisplayName("sollecito breve: invia il blocco e registra una notifica per posizione")
    void sollecitoBreve() {
        List<Posizione> blocco = List.of(posizione("IUV-1"), posizione("IUV-2"));
        when(posizioneRepository.findDaNotificare(oggi, oggi.plusDays(BREVE), TIPO_BREVE)).thenReturn(blocco);

        service.inviaNotifiche();

        verify(sistemaPostale).inviaBlocco(blocco);
        assertThat(notificheSalvate(2))
                .extracting(Notifica::getIuv, Notifica::getTipo, Notifica::getDataInvio)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("IUV-1", TIPO_BREVE, oggi),
                        org.assertj.core.groups.Tuple.tuple("IUV-2", TIPO_BREVE, oggi));
    }

    @Test
    @DisplayName("sollecito lungo: invia il blocco e registra le notifiche con il tipo lungo")
    void sollecitoLungo() {
        List<Posizione> blocco = List.of(posizione("IUV-3"));
        when(posizioneRepository.findDaNotificare(oggi.plusDays(BREVE), oggi.plusDays(LUNGO), TIPO_LUNGO))
                .thenReturn(blocco);

        service.inviaNotifiche();

        verify(sistemaPostale).inviaBlocco(blocco);
        assertThat(notificheSalvate(1))
                .extracting(Notifica::getIuv, Notifica::getTipo)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("IUV-3", TIPO_LUNGO));
    }

    @Test
    @DisplayName("entrambi i blocchi: due invii separati, uno per tipo")
    void entrambiIBlocchi() {
        List<Posizione> breve = List.of(posizione("IUV-1"));
        List<Posizione> lungo = List.of(posizione("IUV-2"));
        when(posizioneRepository.findDaNotificare(oggi, oggi.plusDays(BREVE), TIPO_BREVE)).thenReturn(breve);
        when(posizioneRepository.findDaNotificare(oggi.plusDays(BREVE), oggi.plusDays(LUNGO), TIPO_LUNGO))
                .thenReturn(lungo);

        service.inviaNotifiche();

        InOrder ordine = inOrder(sistemaPostale);
        ordine.verify(sistemaPostale).inviaBlocco(breve);
        ordine.verify(sistemaPostale).inviaBlocco(lungo);
        assertThat(notificheSalvate(2)).extracting(Notifica::getTipo).containsExactly(TIPO_BREVE, TIPO_LUNGO);
    }

    @Test
    @DisplayName("prima invia la posta, poi registra le notifiche")
    void ordineInvioRegistrazione() {
        List<Posizione> blocco = List.of(posizione("IUV-1"));
        when(posizioneRepository.findDaNotificare(oggi, oggi.plusDays(BREVE), TIPO_BREVE)).thenReturn(blocco);

        service.inviaNotifiche();

        InOrder ordine = inOrder(sistemaPostale, notificaRepository);
        ordine.verify(sistemaPostale).inviaBlocco(blocco);
        ordine.verify(notificaRepository).save(any(Notifica.class));
    }

    @Test
    @DisplayName("le notifiche salvate sono nuove (id null): il service non aggiorna notifiche esistenti")
    void notificheNuove() {
        when(posizioneRepository.findDaNotificare(oggi, oggi.plusDays(BREVE), TIPO_BREVE))
                .thenReturn(List.of(posizione("IUV-1")));

        service.inviaNotifiche();

        assertThat(notificheSalvate(1).get(0).getId()).isNull();
    }


    @Test
    @DisplayName("errore del sistema postale: nessuna notifica registrata, il sollecito lungo non parte")
    void erroreInvio() {
        List<Posizione> blocco = List.of(posizione("IUV-1"));
        when(posizioneRepository.findDaNotificare(oggi, oggi.plusDays(BREVE), TIPO_BREVE)).thenReturn(blocco);
        doThrow(new IllegalStateException("posta non disponibile")).when(sistemaPostale).inviaBlocco(blocco);

        assertThatThrownBy(() -> service.inviaNotifiche()).isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(notificaRepository);
        verify(posizioneRepository, never()).findDaNotificare(oggi.plusDays(BREVE), oggi.plusDays(LUNGO), TIPO_LUNGO);
    }

    @Test
    @DisplayName("errore nel salvataggio: la posta è già partita, l'eccezione esce")
    void erroreSalvataggio() {
        List<Posizione> blocco = List.of(posizione("IUV-1"), posizione("IUV-2"));
        when(posizioneRepository.findDaNotificare(oggi, oggi.plusDays(BREVE), TIPO_BREVE)).thenReturn(blocco);
        when(notificaRepository.save(any(Notifica.class))).thenThrow(new IllegalStateException("db"));

        assertThatThrownBy(() -> service.inviaNotifiche()).isInstanceOf(IllegalStateException.class);

        verify(sistemaPostale).inviaBlocco(blocco);
        verify(notificaRepository, times(1)).save(any(Notifica.class));
    }

    @Test
    @DisplayName("errore del repository: l'eccezione esce, nessun invio")
    void erroreRepository() {
        when(posizioneRepository.findDaNotificare(oggi, oggi.plusDays(BREVE), TIPO_BREVE))
                .thenThrow(new IllegalStateException("db"));

        assertThatThrownBy(() -> service.inviaNotifiche()).isInstanceOf(IllegalStateException.class);

        verify(sistemaPostale, never()).inviaBlocco(anyList());
    }


    private List<Notifica> notificheSalvate(int quante) {
        ArgumentCaptor<Notifica> captor = ArgumentCaptor.forClass(Notifica.class);
        verify(notificaRepository, times(quante)).save(captor.capture());
        return captor.getAllValues();
    }

    private static Posizione posizione(String iuv) {
        Posizione p = new Posizione();
        p.setIuv(iuv);
        p.setStato("DA_PAGARE");
        return p;
    }
}