package it.pa.tributi.tributiservice.service;


import it.pa.tributi.tributiservice.dto.DebitoRequest;
import it.pa.tributi.tributiservice.dto.PosizioneDTO;
import it.pa.tributi.tributiservice.entity.Ente;
import it.pa.tributi.tributiservice.entity.Posizione;
import it.pa.tributi.tributiservice.exception.ErroreTributi;
import it.pa.tributi.tributiservice.exception.TributiException;
import it.pa.tributi.tributiservice.mapper.PosizioneMapper;
import it.pa.tributi.tributiservice.repository.EnteRepository;
import it.pa.tributi.tributiservice.repository.PosizioneRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PosizioneService {

    private final PosizioneRepository posizioneRepository;
    private final PosizioneMapper posizioneMapper;
    private final EnteRepository   enteRepository;
    private final GeneratoreIuv generatoreIuv;
    private final int giorniMinimiScadenza;


    public PosizioneService(PosizioneRepository posizioneRepository,
                            PosizioneMapper posizioneMapper,
                            EnteRepository enteRepository,
                            GeneratoreIuv generatoreIuv,
                            @Value("${tributi.giorni-minimi-scadenza}")int giorniMinimiScadenza) {
        this.posizioneRepository = posizioneRepository;
        this.posizioneMapper = posizioneMapper;
        this.enteRepository = enteRepository;
        this.generatoreIuv = generatoreIuv;
        this.giorniMinimiScadenza = giorniMinimiScadenza;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void segnaInCorso(String iuv) {
        Posizione posizione = posizioneRepository.findByIuv(iuv)
                .orElseThrow(() -> new TributiException(ErroreTributi.IUV_NON_TROVATO));
        posizione.setStato("IN_CORSO");
    }

    @Transactional(readOnly = true)
    public List<PosizioneDTO> verificaPosizione(String codiceFiscale) {
        if (codiceFiscale == null || codiceFiscale.isBlank()) {
            throw new TributiException(ErroreTributi.PARAMETRI_MANCANTI);
        }
        return posizioneMapper.toDTO(posizioneRepository.findByCodiceFiscale(codiceFiscale));
    }

    @Transactional
    public String creaPosizione(DebitoRequest richiesta) {

        if (richiesta.dataScadenza().isBefore(LocalDate.now().plusDays(giorniMinimiScadenza))) {
            throw new TributiException(ErroreTributi.SCADENZA_NON_VALIDA);
        }

        Ente ente = enteRepository.findById(richiesta.codiceEnte())
                .orElseThrow(() -> new TributiException(ErroreTributi.ENTE_NON_TROVATO));

        Posizione posizione = Posizione.nuova(
                generatoreIuv.genera(),
                richiesta.codiceFiscale(),
                richiesta.tipoTributo(),
                richiesta.importo(),
                richiesta.dataScadenza(),
                ente);

        return posizioneRepository.save(posizione).getIuv();
    }
}
