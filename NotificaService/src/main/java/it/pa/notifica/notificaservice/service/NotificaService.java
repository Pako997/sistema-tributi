package it.pa.notifica.notificaservice.service;

import it.pa.notifica.notificaservice.entity.Notifica;
import it.pa.notifica.notificaservice.entity.Posizione;
import it.pa.notifica.notificaservice.enums.TipoNotifica;
import it.pa.notifica.notificaservice.external.SistemaPostale;
import it.pa.notifica.notificaservice.repository.NotificaRepository;
import it.pa.notifica.notificaservice.repository.PosizioneRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class NotificaService {

    private final PosizioneRepository posizioneRepository;
    private final NotificaRepository notificaRepository;
    private final SistemaPostale sistemaPostale;

    private final int limMinimo;
    private final int limMassimo;

    public NotificaService(PosizioneRepository posizioneRepository,
                            NotificaRepository notificaRepository,
                            SistemaPostale sistemaPostale,
                            @Value("${notifiche.giorni-sollecito-breve}") int limMinimo,
                            @Value("${notifiche.giorni-sollecito-lungo}") int limMassimo) {
        this.posizioneRepository = posizioneRepository;
        this.notificaRepository = notificaRepository;
        this.sistemaPostale = sistemaPostale;
        this.limMinimo = limMinimo;
        this.limMassimo = limMassimo;
    }


    @Transactional
    public void inviaNotifiche() {
        elabora(0, limMinimo, TipoNotifica.SOLLECITO_BREVE);
        elabora(limMinimo, limMassimo, TipoNotifica.SOLLECITO_LUNGO);
    }

    private void elabora(int giorniMin, int giorniMax, TipoNotifica tipo) {
        LocalDate oggi = LocalDate.now();
        List<Posizione> posizioni = posizioneRepository.findDaNotificare(
                oggi.plusDays(giorniMin), oggi.plusDays(giorniMax), tipo.name());

        if (posizioni.isEmpty()) {
            return;
        }

        sistemaPostale.inviaBlocco(posizioni);
        posizioni.forEach(p -> registra(p.getIuv(), tipo));
    }

    private void registra(String iuv, TipoNotifica tipo) {
        Notifica notifica = new Notifica();
        notifica.setIuv(iuv);
        notifica.setTipo(tipo.name());
        notifica.setDataInvio(LocalDate.now());
        notificaRepository.save(notifica);
    }
}
