package it.pa.tributi.dao;

import it.pa.tributi.exception.TributiException;
import java.util.Date;

public interface IPagamentoDAO {
    void registraPagamento(String iuv, String idTransazione, Date dataPagamento) throws TributiException;
}
