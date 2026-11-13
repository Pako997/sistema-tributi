package it.pa.tributi.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ente")
public class Ente {

    @Id
    @Column(name = "codice_ente", length = 50)
    private String codiceEnte;

    @Column(name = "denominazione", nullable = false)
    private String denominazione;

    @Column(name = "contatto", length = 100)
    private String contatto;

    public String getCodiceEnte() { return codiceEnte; }
    public void setCodiceEnte(String codiceEnte) { this.codiceEnte = codiceEnte; }

    public String getDenominazione() { return denominazione; }
    public void setDenominazione(String denominazione) { this.denominazione = denominazione; }

    public String getContatto() { return contatto; }
    public void setContatto(String contatto) { this.contatto = contatto; }
}