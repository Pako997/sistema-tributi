package it.pa.tributi.tributiservice.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebitoRequest(

        @NotBlank
        String codiceFiscale,

        @NotBlank
        String tipoTributo,

        @NotNull
        @Positive
        BigDecimal importo,

        @NotNull
        @Future
        LocalDate dataScadenza,

        @NotBlank
        String codiceEnte
) {
}
