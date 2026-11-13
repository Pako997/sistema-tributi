package it.pa.tributi.tributiservice.dto;

import java.math.BigDecimal;

public record PagamentoRequest(String iuv, BigDecimal importoVersato) {
}
