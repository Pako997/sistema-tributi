package it.pa.tributi.dao;

import it.pa.tributi.entity.Ente;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class EnteDAO implements IEnteDAO{

    @PersistenceContext(unitName = "tributiPU")
    private EntityManager em;

    @Override
    public Ente getEnteByCodice(String codiceEnte) throws TributiException {

        Ente ente = null;

        try {
            ente =  em.find(Ente.class,codiceEnte);

            if (ente != null){
                return ente;
            }else {
                throw new TributiException(CodiciErrore.CODICE_NOTFOUND, CodiciErrore.MESSAGGIO_ENTE_NOTFOUND);
            }
        }catch (TributiException e){
            throw e;
        }catch (Exception e){
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO, CodiciErrore.MESSAGGIO_ERRORE_INTERNO,e);
        }
    }
}
