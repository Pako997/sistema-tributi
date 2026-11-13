package it.pa.notifica.notificaservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "posizione")
@Data
public class Posizione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iuv", length = 35, nullable = false, unique = true)
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
    private LocalDate dataEmissione;

    @Column(name = "data_scadenza", nullable = false)
    private LocalDate dataScadenza;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @ManyToOne
    @JoinColumn(name = "codice_ente", nullable = false)
    private Ente ente;

}
