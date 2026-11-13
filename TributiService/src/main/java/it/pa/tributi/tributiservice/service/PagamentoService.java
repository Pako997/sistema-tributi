package it.pa.tributi.tributiservice.service;

import it.pa.tributi.tributiservice.dto.PosizioneDTO;
import it.pa.tributi.tributiservice.dto.RicevutaDTO;
import it.pa.tributi.tributiservice.entity.ArchivioPagamento;
import it.pa.tributi.tributiservice.entity.Posizione;
import it.pa.tributi.tributiservice.exception.ErroreTributi;
import it.pa.tributi.tributiservice.exception.TributiException;
import it.pa.tributi.tributiservice.external.SistemaPagamento;
import it.pa.tributi.tributiservice.mapper.PosizioneMapper;
import it.pa.tributi.tributiservice.mapper.RicevutaMapper;
import it.pa.tributi.tributiservice.repository.ArchivioRepository;
import it.pa.tributi.tributiservice.repository.PosizioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PagamentoService {

    private static final String STATO_PAGATA = "PAGATA";

    private final PosizioneRepository posizioneRepository;
    private final ArchivioRepository archivioRepository;
    private final SistemaPagamento sistemaPagamento;
    private final RicevutaMapper ricevutaMapper;
    private final PosizioneMapper posizioneMapper;


    public PagamentoService(PosizioneRepository posizioneRepository,
                            ArchivioRepository archivioRepository,
                            SistemaPagamento sistemaPagamento,
                            RicevutaMapper ricevutaMapper,
                            PosizioneMapper posizioneMapper) {
        this.posizioneRepository = posizioneRepository;
        this.archivioRepository = archivioRepository;
        this.sistemaPagamento = sistemaPagamento;
        this.ricevutaMapper = ricevutaMapper;
        this.posizioneMapper = posizioneMapper;
    }

    @Transactional
    public RicevutaDTO eseguiPagamento(String iuv, BigDecimal importoVersato) {

        Posizione posizione = posizioneRepository.findByIuv(iuv)
                .orElseThrow(() -> new TributiException(ErroreTributi.IUV_NON_TROVATO));
        PosizioneDTO posizioneDTO = posizioneMapper.toDTO(posizione);

        String idTransazione = sistemaPagamento.autorizza(posizioneDTO, importoVersato);
        if (idTransazione == null) {
            throw new TributiException(ErroreTributi.PAGAMENTO_NON_RIUSCITO);
        }

        LocalDate dataPagamento = LocalDate.now();

        posizione.setStato(STATO_PAGATA);
        posizione.setDataPagamento(dataPagamento);

        archivioRepository.save(archivio(iuv, idTransazione, dataPagamento));

        return ricevutaMapper.toRicevuta(posizione, idTransazione, importoVersato, dataPagamento);
    }

    private ArchivioPagamento archivio(String iuv, String idTransazione, LocalDate data) {
        ArchivioPagamento archivio = new ArchivioPagamento();
        archivio.setIuv(iuv);
        archivio.setIdTransazione(idTransazione);
        archivio.setDataPagamento(data);
        return archivio;
    }


}
