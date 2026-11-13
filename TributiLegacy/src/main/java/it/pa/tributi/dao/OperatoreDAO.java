package it.pa.tributi.dao;

import it.pa.tributi.entity.Operatore;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class OperatoreDAO implements IOperatoreDAO{

    @PersistenceContext(unitName = "tributiPU")
    private EntityManager em;


    @Override
    public String findOperatoreByCodiceFiscale(String codiceFiscale) {
        Operatore operatore = em.find(Operatore.class, codiceFiscale);
        if (operatore != null) {
            return "OPERATORE";
        }
        return "CITTADINO";
    }
}
