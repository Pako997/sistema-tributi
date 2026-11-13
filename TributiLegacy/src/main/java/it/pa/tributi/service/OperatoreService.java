package it.pa.tributi.service;

import it.pa.tributi.context.ServiceLocator;
import it.pa.tributi.dao.IOperatoreDAO;

import javax.ejb.Stateless;
import javax.management.ServiceNotFoundException;

@Stateless
public class OperatoreService implements IOperatoreService{
    @Override
    public String getRuolo(String codiceFiscale) throws ServiceNotFoundException {
        IOperatoreDAO dao = ServiceLocator.get(IOperatoreDAO.class);

        return dao.findOperatoreByCodiceFiscale(codiceFiscale);
    }
}
