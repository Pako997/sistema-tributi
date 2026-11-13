package it.pa.tributi.service;

import it.pa.tributi.dto.RicevutaDTO;
import it.pa.tributi.exception.TributiException;

import javax.management.ServiceNotFoundException;

public interface IPagamentoService {
    public RicevutaDTO eseguiPagamento(String iuv, Double importoVersato) throws ServiceNotFoundException, TributiException;
}
