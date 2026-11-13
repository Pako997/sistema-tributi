package it.pa.tributi.tributiservice.mapper;

import it.pa.tributi.tributiservice.dto.RicevutaDTO;
import it.pa.tributi.tributiservice.entity.Posizione;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class RicevutaMapper {

    public RicevutaDTO toRicevuta(Posizione posizione, String idTransazione,
                                  BigDecimal importoVersato, LocalDate dataPagamento) {
        RicevutaDTO ricevuta = new RicevutaDTO();
        ricevuta.setCodiceFiscale(posizione.getCodiceFiscale());
        ricevuta.setTipoTributo(posizione.getTipoTributo());
        ricevuta.setIuv(posizione.getIuv());
        ricevuta.setIdTransazione(idTransazione);
        ricevuta.setImporto(importoVersato);
        ricevuta.setCodiceEnte(posizione.getEnte().getCodiceEnte());
        ricevuta.setEnteEmittente(posizione.getEnte().getDenominazione());
        ricevuta.setContattoEnte(posizione.getEnte().getContatto());
        ricevuta.setDataPagamento(dataPagamento);
        return ricevuta;
    }
}
