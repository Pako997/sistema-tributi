package it.pa.tributi.tributiservice.mapper;

import it.pa.tributi.tributiservice.dto.PosizioneDTO;
import it.pa.tributi.tributiservice.entity.Posizione;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PosizioneMapper {

    public PosizioneDTO toDTO(Posizione posizione) {
        PosizioneDTO dto = new PosizioneDTO();
        dto.setIuv(posizione.getIuv());
        dto.setCodiceFiscale(posizione.getCodiceFiscale());
        dto.setTipoTributo(posizione.getTipoTributo());
        dto.setImporto(posizione.getImporto());
        dto.setImportoOriginale(posizione.getImportoOriginale());
        dto.setStato(posizione.getStato());
        dto.setDataEmissione(posizione.getDataEmissione());
        dto.setDataScadenza(posizione.getDataScadenza());
        dto.setDataPagamento(posizione.getDataPagamento());
        dto.setCodiceEnte(posizione.getEnte().getCodiceEnte());
        dto.setEnteEmittente(posizione.getEnte().getDenominazione());
        dto.setContattoEnte(posizione.getEnte().getContatto());
        return dto;
    }

    public List<PosizioneDTO> toDTO(List<Posizione> posizioni) {
        return posizioni.stream().map(this::toDTO).toList();
    }
}
