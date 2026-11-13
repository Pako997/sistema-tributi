package it.pa.tributi.dao;

import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.exception.TributiException;

import java.math.BigDecimal;
import java.util.List;

public interface IBatchNotificaDAO {
    List<PosizioneDTO> findPosizioniDaNotificare(int giorniAllaScadenza, String tipoNotifica)
            throws TributiException;
    List<PosizioneDTO> findPosizioniDaNotificareEmissione() throws TributiException;

    List<PosizioneDTO> findPosizioniDaNotificare(int giorniMin, int giorniMax, String tipoNotifica)
            throws TributiException;

    List<PosizioneDTO> findPosizioniScadute() throws TributiException;
    void aggiornaImporto(String iuv, BigDecimal nuovoImporto) throws TributiException;
}
