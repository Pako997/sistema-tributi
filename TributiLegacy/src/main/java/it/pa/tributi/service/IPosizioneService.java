package it.pa.tributi.service;

import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.exception.TributiException;

import javax.management.ServiceNotFoundException;
import java.math.BigDecimal;
import java.util.List;

public interface IPosizioneService {

    public void aggiornaPosizioneInCorso(String iuv) throws ServiceNotFoundException, TributiException;
    public List<PosizioneDTO> verificaPosizione(String codiceFiscale) throws ServiceNotFoundException;
    public  String createPagamento( String codiceFiscale,
                                    String tipoTributo,
                                    BigDecimal importo,
                                    //stato DA_PAGARE
                                    //String dataEmissione data oggi +1
                                    String dataScadenza,
                                    String codiceEnte) throws ServiceNotFoundException, TributiException;
}
