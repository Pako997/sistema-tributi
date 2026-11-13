package it.pa.tributi.dao;

import it.pa.tributi.entity.Ente;
import it.pa.tributi.exception.TributiException;

public interface IEnteDAO {
    Ente getEnteByCodice(String codiceEnte) throws TributiException;
}
