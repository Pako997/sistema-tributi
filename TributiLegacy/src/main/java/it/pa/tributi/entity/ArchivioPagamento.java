package it.pa.tributi.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "archivio_pagamenti")
public class ArchivioPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iuv", length = 35, unique = true, nullable = false)
    private String iuv;

    @Column(name = "id_transazione", length = 35, unique = true, nullable = false)
    private String idTransazione;

    @Column(name = "data_pagamento", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dataPagamento;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIuv() { return iuv; }
    public void setIuv(String iuv) { this.iuv = iuv; }

    public String getIdTransazione() { return idTransazione; }
    public void setIdTransazione(String idTransazione) { this.idTransazione = idTransazione; }

    public Date getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(Date dataPagamento) { this.dataPagamento = dataPagamento; }
}
