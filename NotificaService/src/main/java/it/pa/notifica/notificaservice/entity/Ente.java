package it.pa.notifica.notificaservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ente")
@Data
public class Ente {

    @Id
    @Column(name = "codice_ente", length = 50)
    private String codiceEnte;

    @Column(name = "denominazione", nullable = false)
    private String denominazione;

    @Column(name = "contatto", length = 100)
    private String contatto;

}
