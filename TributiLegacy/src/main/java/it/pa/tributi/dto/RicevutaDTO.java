package it.pa.tributi.dto;

import java.util.Date;

public class RicevutaDTO {

    private String codiceFiscale;
    private String tipoTributo;
    private String iuv;
    private String idTransazione;
    private Double importo;
    private String codiceEnte;
    private String enteEmittente;
    private String contattoEnte;
    private Date dataPagamento;

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getTipoTributo() {
        return tipoTributo;
    }

    public void setTipoTributo(String tipoTributo) {
        this.tipoTributo = tipoTributo;
    }

    public String getIuv() {
        return iuv;
    }

    public void setIuv(String iuv) {
        this.iuv = iuv;
    }

    public String getIdTransazione() {
        return idTransazione;
    }

    public void setIdTransazione(String idTransazione) {
        this.idTransazione = idTransazione;
    }

    public Double getImporto() {
        return importo;
    }

    public void setImporto(Double importo) {
        this.importo = importo;
    }

    public String getCodiceEnte() {
        return codiceEnte;
    }

    public void setCodiceEnte(String codiceEnte) {
        this.codiceEnte = codiceEnte;
    }

    public String getEnteEmittente() {
        return enteEmittente;
    }

    public void setEnteEmittente(String enteEmittente) {
        this.enteEmittente = enteEmittente;
    }

    public String getContattoEnte() {
        return contattoEnte;
    }

    public void setContattoEnte(String contattoEnte) {
        this.contattoEnte = contattoEnte;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
}
