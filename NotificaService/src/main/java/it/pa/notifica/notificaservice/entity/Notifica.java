package it.pa.notifica.notificaservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "notifica")
@Data
public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iuv", length = 35, nullable = false)
    private String iuv;

    @Column(name = "data_invio", nullable = false)
    private LocalDate dataInvio;

    @Column(name = "tipo", length = 20, nullable = false)
    private String tipo;

}
