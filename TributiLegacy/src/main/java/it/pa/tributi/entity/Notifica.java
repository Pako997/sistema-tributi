package it.pa.tributi.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "notifica")
public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iuv", length = 35, nullable = false)
    private String iuv;

    @Column(name = "data_invio", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dataInvio;

    @Column(name = "tipo", length = 20, nullable = false)
    private String tipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIuv() { return iuv; }
    public void setIuv(String iuv) { this.iuv = iuv; }

    public Date getDataInvio() { return dataInvio; }
    public void setDataInvio(Date dataInvio) { this.dataInvio = dataInvio; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
