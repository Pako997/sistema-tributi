package it.pa.notifica.notificaservice.strategy;

import it.pa.notifica.notificaservice.entity.Posizione;

import java.math.BigDecimal;

public interface StrategiaRivalutazione {

    String tipoTributo();

    BigDecimal calcolaImporto(Posizione posizione, long giorniRitardo);
}
