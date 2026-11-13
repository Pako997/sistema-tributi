package it.pa.tributi.service;

import it.pa.tributi.context.ServiceLocator;
import it.pa.tributi.dao.IEnteDAO;
import it.pa.tributi.dao.IPosizioneDAO;
import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.entity.Ente;
import it.pa.tributi.entity.Posizione;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;

import javax.ejb.Stateless;
import javax.management.ServiceNotFoundException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Stateless
public class PosizioneService implements IPosizioneService{


    @Override
    public void aggiornaPosizioneInCorso(String iuv) throws ServiceNotFoundException, TributiException {
        IPosizioneDAO dao = ServiceLocator.get(IPosizioneDAO.class);
        dao.aggiornaPosizioneInCorso(iuv);
    }

    @Override
    public List<PosizioneDTO> verificaPosizione(String codiceFiscale) throws ServiceNotFoundException {
        //per il refactoring di questo metodo, il repo restituirà entity, il mapping Entity DTO avrà luogo in un metodo del service
        IPosizioneDAO dao = ServiceLocator.get(IPosizioneDAO.class);

        List<PosizioneDTO> res = dao.findPosizioniByCodiceFiscale(codiceFiscale);
        return res;
    }

    @Override
    public String createPagamento(String codiceFiscale, String tipoTributo, BigDecimal importo, String dataScadenza, String codiceEnte) throws ServiceNotFoundException, TributiException {
        String iuvPosizione = null;
        try{
            IEnteDAO enteDAO = ServiceLocator.get(IEnteDAO.class);
            Ente ente = enteDAO.getEnteByCodice(codiceEnte);

            //generazione iuv, fare metodo apposta in refactoring
            String timestamp = String.valueOf(System.currentTimeMillis());
            String random = String.format("%04d", new Random().nextInt(10000));
            String iuv = timestamp + random;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateEmissione = new Date();
            Date dateScadenza = sdf.parse(dataScadenza);

            IPosizioneDAO posizioneDAO = ServiceLocator.get(IPosizioneDAO.class);

            Posizione posizioneGenerata = posizioneDAO.createPagamento(iuv, codiceFiscale,  tipoTributo,  importo,importo, "DA_PAGARE" ,dateEmissione,dateScadenza, ente);
            iuvPosizione = posizioneGenerata.getIuv();
        }catch (TributiException e){
            throw e;
        }catch (Exception e){
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO, CodiciErrore.MESSAGGIO_ERRORE_INTERNO,e);
        }

        return iuvPosizione;
    }
}
