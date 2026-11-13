package it.pa.tributi.dto;


import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Date;

@XmlRootElement
public class PosizioneDTO {
    //identificativo univoco versamento
    private String iuv;
    private String codiceFiscale;
    private String tipoTributo;// TARI, IMU, MULTA, TICKET
    private BigDecimal importo;
    private BigDecimal importoOriginale;
    private String stato;// PAGATA, DA_PAGARE, SCADUTA
    private Date dataEmissione;
    private Date dataScadenza;
    private Date dataPagamento;
    private String codiceEnte;//  codice identificativo univoco dell'ente
    private String enteEmittente;//  denominazione semplice di lettura dell'ente
    private String contattoEnte;//  contatto dell ente

    public String getIuv() {
        return iuv;
    }

    public void setIuv(String iuv) {
        this.iuv = iuv;
    }

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

    public BigDecimal getImporto() {
        return importo;
    }

    public void setImporto(BigDecimal importo) {
        this.importo = importo;
    }

    public BigDecimal getImportoOriginale() {
        return importoOriginale;
    }

    public void setImportoOriginale(BigDecimal importoOriginale) {
        this.importoOriginale = importoOriginale;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public Date getDataEmissione() {
        return dataEmissione;
    }

    public void setDataEmissione(Date dataEmissione) {
        this.dataEmissione = dataEmissione;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getCodiceEnte() {
        return codiceEnte;
    }

    public void setCodiceEnte(String codiceEnte) {
        this.codiceEnte = codiceEnte;
    }

    public String getContattoEnte() {
        return contattoEnte;
    }

    public void setContattoEnte(String contattoEnte) {
        this.contattoEnte = contattoEnte;
    }

    public String getEnteEmittente() {
        return enteEmittente;
    }

    public void setEnteEmittente(String enteEmittente) {
        this.enteEmittente = enteEmittente;
    }

    public Date getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(Date dataScadenza) {
        this.dataScadenza = dataScadenza;
    }
}
