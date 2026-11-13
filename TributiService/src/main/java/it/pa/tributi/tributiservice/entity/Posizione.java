package it.pa.tributi.tributiservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "posizione")
@Getter
@Setter
public class Posizione {

    private static final String STATO_DA_PAGARE = "DA_PAGARE";

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
    private LocalDate  dataEmissione;

    @Column(name = "data_scadenza", nullable = false)
    private LocalDate  dataScadenza;

    @Column(name = "data_pagamento", nullable = true)
    private LocalDate dataPagamento;

    @ManyToOne
    @JoinColumn(name = "codice_ente")
    private Ente ente;


    public static Posizione nuova(String iuv,
                                  String codiceFiscale,
                                  String tipoTributo,
                                  BigDecimal importo,
                                  LocalDate dataScadenza,
                                  Ente ente) {

        Posizione posizione = new Posizione();
        posizione.iuv = iuv;
        posizione.codiceFiscale = codiceFiscale;
        posizione.tipoTributo = tipoTributo;
        posizione.importo = importo;
        posizione.importoOriginale = importo;
        posizione.stato = STATO_DA_PAGARE;
        posizione.dataEmissione = LocalDate.now();
        posizione.dataScadenza = dataScadenza;
        posizione.ente = ente;
        return posizione;
    }
}