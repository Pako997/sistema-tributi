package it.pa.tributi.service;

import javax.management.ServiceNotFoundException;

public interface IOperatoreService {
    public String getRuolo(String codiceFiscale) throws ServiceNotFoundException;
}
