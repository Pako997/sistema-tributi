package it.pa.tributi.service;

import it.pa.tributi.context.ServiceLocator;
import it.pa.tributi.dao.IPagamentoDAO;
import it.pa.tributi.dao.IPosizioneDAO;
import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.dto.RicevutaDTO;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;
import it.pa.tributi.external.ISistemaPagamento;

import javax.ejb.Stateless;
import javax.management.ServiceNotFoundException;
import java.util.Date;

@Stateless
public class PagamentoService implements IPagamentoService{
    @Override
    public RicevutaDTO eseguiPagamento(String iuv, Double importoVersato) throws ServiceNotFoundException, TributiException {
        //1. verifica iuv e posizione che sia un importo da pagare (tramite iuv)
        IPosizioneDAO pdao = ServiceLocator.get(IPosizioneDAO.class);
        PosizioneDTO posizione = pdao.findPosizioneByIUV(iuv); // se non trova lancia ecc

        Date dataPagamento = new Date();

        //2. faccio il tocco presso il sistema di pagamento e genero PSP
        ISistemaPagamento payServ = ServiceLocator.get(ISistemaPagamento.class);
        String idTransazione = payServ.verificaPagamento(posizione, importoVersato);
        if (idTransazione == null)
            throw new TributiException(CodiciErrore.CODICE_PAGAMENTO_NON_RIUSCITO, CodiciErrore.MESSAGGIO_PAGAMENTO_NON_RIUSCITO);

        //3. update della posizione estratta prima e salvataggio pagamento
        pdao.aggiornaPosizionePagata(iuv,dataPagamento);
        IPagamentoDAO payDao = ServiceLocator.get(IPagamentoDAO.class);
        payDao.registraPagamento(posizione.getIuv(), idTransazione, dataPagamento);

        //4. scrivo la ricevuta
        RicevutaDTO ricevuta = new RicevutaDTO();
        ricevuta.setCodiceFiscale(posizione.getCodiceFiscale());
        ricevuta.setTipoTributo(posizione.getTipoTributo());
        ricevuta.setIuv(posizione.getIuv());
        ricevuta.setIdTransazione(idTransazione);
        ricevuta.setImporto(importoVersato);
        ricevuta.setCodiceEnte(posizione.getCodiceEnte());
        ricevuta.setEnteEmittente(posizione.getEnteEmittente());
        ricevuta.setContattoEnte(posizione.getContattoEnte());
        ricevuta.setDataPagamento(dataPagamento);

        return ricevuta;
    }
}
