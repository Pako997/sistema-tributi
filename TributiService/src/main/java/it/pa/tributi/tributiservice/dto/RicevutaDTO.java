package it.pa.tributi.tributiservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RicevutaDTO {

    private String codiceFiscale;
    private String tipoTributo;
    private String iuv;
    private String idTransazione;
    private BigDecimal importo;
    private String codiceEnte;
    private String enteEmittente;
    private String contattoEnte;
    private LocalDate dataPagamento;
}
