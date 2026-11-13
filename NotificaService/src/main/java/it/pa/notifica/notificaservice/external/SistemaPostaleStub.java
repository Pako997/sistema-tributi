package it.pa.notifica.notificaservice.external;

import it.pa.notifica.notificaservice.entity.Posizione;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SistemaPostaleStub implements SistemaPostale {

    private static final Logger log = LoggerFactory.getLogger(SistemaPostaleStub.class);

    @Override
    public void inviaBlocco(List<Posizione> blocco) {
        blocco.forEach(p -> log.info("Invio avviso {} a {} - scadenza {}",
                p.getIuv(), p.getCodiceFiscale(), p.getDataScadenza()));
    }
}