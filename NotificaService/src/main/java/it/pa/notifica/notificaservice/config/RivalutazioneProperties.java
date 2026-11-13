package it.pa.notifica.notificaservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "rivalutazione")
@Data
public class RivalutazioneProperties {
    private int multaGiorniTermine;
    private int multaGiorniSemestre;
    private BigDecimal multaMaggiorazioneSemestrale;
    private BigDecimal multaTettoMaggiorazione;
    private BigDecimal tassoLegale;
}
