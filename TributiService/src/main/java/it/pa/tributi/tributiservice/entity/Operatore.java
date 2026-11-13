package it.pa.tributi.tributiservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "operatore")
@Getter
@Setter
public class Operatore {

    @Id
    @Column(name = "codice_fiscale", length = 16)
    private String codiceFiscale;

    @Column(name = "codice_sede", length = 50)
    private String codiceSede;
}
