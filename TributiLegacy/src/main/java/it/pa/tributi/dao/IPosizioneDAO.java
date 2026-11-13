package it.pa.tributi.dao;

import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.entity.Ente;
import it.pa.tributi.entity.Posizione;
import it.pa.tributi.exception.TributiException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface IPosizioneDAO {

    void aggiornaPosizioneInCorso(String iuv) throws TributiException;

    void aggiornaPosizionePagata(String iuv, Date dataPagamento) throws TributiException;

    PosizioneDTO findPosizioneByIUV(String iuv) throws TributiException;

    List<PosizioneDTO> findPosizioniByCodiceFiscale(String codiceFiscale);

    public Posizione createPagamento(String iuv,
                                     String codiceFiscale,
                                     String tipoTributo,
                                     BigDecimal importo,
                                     BigDecimal importoOriginale,
                                     String stato,
                                     Date dataEmissione,
                                     Date dataScadenza,
                                     Ente ente
    );
}
