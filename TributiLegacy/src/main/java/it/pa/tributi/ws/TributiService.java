package it.pa.tributi.ws;

import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.dto.RicevutaDTO;
import it.pa.tributi.entity.Notifica;
import it.pa.tributi.exception.TributiException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.management.ServiceNotFoundException;
import java.math.BigDecimal;
import java.util.List;

@WebService
public interface TributiService {

    @WebMethod
    List<Notifica> visualNotifiche() throws ServiceNotFoundException;

    @WebMethod
    List<PosizioneDTO> verificaPosizione(
            @WebParam(name = "codiceFiscale") String codiceFiscale
    )  throws TributiException;

    @WebMethod
    RicevutaDTO eseguiPagamento(
            @WebParam(name = "iuv") String iuv,
            @WebParam(name = "importoVersato") Double importoVersato
    )  throws TributiException;

    @WebMethod
    public String registraPagamento(@WebParam(name = "codiceFiscale") String codiceFiscale,
                                    @WebParam(name = "tipoTributo") String tipoTributo,
                                    @WebParam(name = "importo") BigDecimal importo,
                                    @WebParam(name = "dataScadenza") String dataScadenza,
                                    @WebParam(name = "codiceEnte") String codiceEnte
    ) throws TributiException;
}
