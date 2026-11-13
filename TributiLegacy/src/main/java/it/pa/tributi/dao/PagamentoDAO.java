package it.pa.tributi.dao;

import it.pa.tributi.entity.ArchivioPagamento;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;

@Stateless
public class PagamentoDAO implements IPagamentoDAO{

    @PersistenceContext(unitName = "tributiPU")
    private EntityManager em;

    @Override
    public void registraPagamento(String iuv, String idTransazione, Date dataPagamento) throws TributiException {
        try {
            ArchivioPagamento archivio = new ArchivioPagamento();
            archivio.setIuv(iuv);
            archivio.setIdTransazione(idTransazione);
            archivio.setDataPagamento(dataPagamento);
            em.persist(archivio);
            em.flush();

        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
    }
}
