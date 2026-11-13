package it.pa.tributi.tributiservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "archivio_pagamenti")
@Getter
@Setter
public class ArchivioPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iuv", length = 35, unique = true, nullable = false)
    private String iuv;

    @Column(name = "id_transazione", length = 35, unique = true, nullable = false)
    private String idTransazione;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;
}
