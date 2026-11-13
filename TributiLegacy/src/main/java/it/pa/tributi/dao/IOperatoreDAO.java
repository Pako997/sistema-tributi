package it.pa.tributi.dao;

import it.pa.tributi.entity.Operatore;

public interface IOperatoreDAO {
    String findOperatoreByCodiceFiscale(String codiceFiscale);
}
