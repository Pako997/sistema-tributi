package it.pa.notifica.notificaservice.external;

import it.pa.notifica.notificaservice.entity.Posizione;

import java.util.List;

public interface SistemaPostale {
    void inviaBlocco(List<Posizione> blocco);
}
