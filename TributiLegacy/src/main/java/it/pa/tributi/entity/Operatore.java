package it.pa.tributi.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import javax.persistence.*;

@Entity
@Table(name = "operatore")
public class Operatore {

    @Id
    @Column(name = "codice_fiscale", length = 16)
    private String codiceFiscale;

    @Column(name = "codice_sede", length = 50)
    private String codiceSede;

    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }

    public String getCodiceSede() { return codiceSede; }
    public void setCodiceSede(String codiceEnte) { this.codiceSede = codiceEnte; }
}
