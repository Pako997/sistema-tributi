package it.pa.tributi.tributiservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ente")
@Getter
@Setter
public class Ente {

    @Id
    @Column(name = "codice_ente", length = 50)
    private String codiceEnte;

    @Column(name = "denominazione", nullable = false)
    private String denominazione;

    @Column(name = "contatto", length = 100)
    private String contatto;
}
