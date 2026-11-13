package it.pa.notifica.notificaservice.service;

import it.pa.notifica.notificaservice.entity.Posizione;
import it.pa.notifica.notificaservice.repository.PosizioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AggiornamentoService {

    private final PosizioneRepository posizioneRepository;
    private final RegistroStrategie registro;

    public AggiornamentoService(PosizioneRepository posizioneRepository,
                                RegistroStrategie registro) {
        this.posizioneRepository = posizioneRepository;
        this.registro = registro;
    }

    @Transactional
    public void rivalutaScadute() {
        List<Posizione> scadute = posizioneRepository.findScadute(LocalDate.now());
        scadute.forEach(this::rivaluta);
    }

    private void rivaluta(Posizione posizione) {
        registro.per(posizione.getTipoTributo()).ifPresent(strategia -> {
            long giorni = ChronoUnit.DAYS.between(posizione.getDataScadenza(), LocalDate.now());
            BigDecimal nuovo = strategia.calcolaImporto(posizione, giorni);

            if (nuovo.compareTo(posizione.getImporto()) != 0) {
                posizione.setImporto(nuovo);
            }
        });
    }
}
