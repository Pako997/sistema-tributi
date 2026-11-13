package it.pa.tributi.tributiservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class PosizioneDTO {
    //identificativo univoco versamento
    private String iuv;
    private String codiceFiscale;
    private String tipoTributo;// TARI, IMU, MULTA, TICKET
    private BigDecimal importo;
    private BigDecimal importoOriginale;
    private String stato;// PAGATA, DA_PAGARE, SCADUTA
    private LocalDate dataEmissione;
    private LocalDate dataScadenza;
    private LocalDate dataPagamento;
    private String codiceEnte;//  codice identificativo univoco dell'ente
    private String enteEmittente;//  denominazione semplice di lettura dell'ente
    private String contattoEnte;//  contatto dell ente
}
