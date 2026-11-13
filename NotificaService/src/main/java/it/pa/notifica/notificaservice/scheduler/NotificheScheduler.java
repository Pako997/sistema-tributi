package it.pa.notifica.notificaservice.scheduler;

import it.pa.notifica.notificaservice.service.AggiornamentoService;
import it.pa.notifica.notificaservice.service.NotificaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificheScheduler {

    private final AggiornamentoService aggiornamentoService;
    private final NotificaService notificaService;

    public NotificheScheduler(AggiornamentoService aggiornamentoService,
                              NotificaService notificaService) {
        this.aggiornamentoService = aggiornamentoService;
        this.notificaService = notificaService;
    }

    @Scheduled(cron = "${notifiche.cron}")
    public void esegui() {
        aggiornamentoService.rivalutaScadute();
        notificaService.inviaNotifiche();
    }
}
