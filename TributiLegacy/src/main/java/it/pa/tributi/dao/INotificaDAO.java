package it.pa.tributi.dao;

import it.pa.tributi.entity.Notifica;
import it.pa.tributi.exception.TributiException;

import java.util.List;

public interface INotificaDAO {
    List<Notifica> visualNotifica();
    void registraNotifica(String iuv, String tipo) throws TributiException;
}
