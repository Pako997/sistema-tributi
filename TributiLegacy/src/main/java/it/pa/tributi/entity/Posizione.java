package it.pa.tributi.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "posizione")
public class Posizione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iuv", length = 35, unique = true, nullable = false)
    private String iuv;

    @Column(name = "codice_fiscale", length = 16, nullable = false)
    private String codiceFiscale;

    @Column(name = "tipo_tributo", length = 50, nullable = false)
    private String tipoTributo;

    @Column(name = "importo", precision = 10, scale = 2, nullable = false)
    private BigDecimal importo;

    @Column(name = "importo_originale", precision = 10, scale = 2, nullable = false)
    private BigDecimal importoOriginale;

    @Column(name = "stato", length = 20, nullable = false)
    private String stato;

    @Column(name = "data_emissione", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dataEmissione;

    @Column(name = "data_scadenza", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dataScadenza;

    @Column(name = "data_pagamento", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date dataPagamento;

    @ManyToOne
    @JoinColumn(name = "codice_ente")
    private Ente ente;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIuv() { return iuv; }
    public void setIuv(String iuv) { this.iuv = iuv; }

    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }

    public String getTipoTributo() { return tipoTributo; }
    public void setTipoTributo(String tipoTributo) { this.tipoTributo = tipoTributo; }

    public BigDecimal getImporto() { return importo; }
    public void setImporto(BigDecimal importo) { this.importo = importo; }

    public BigDecimal getImportoOriginale() { return importoOriginale; }
    public void setImportoOriginale(BigDecimal importoOriginale) { this.importoOriginale = importoOriginale; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public Date getDataEmissione() { return dataEmissione; }
    public void setDataEmissione(Date dataEmissione) { this.dataEmissione = dataEmissione; }

    public Date getDataScadenza() { return dataScadenza; }
    public void setDataScadenza(Date dataScadenza) { this.dataScadenza = dataScadenza; }

    public Date getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(Date dataPagamento) { this.dataPagamento = dataPagamento; }

    public Ente getEnte() { return ente; }
    public void setEnte(Ente ente) { this.ente = ente; }
}
