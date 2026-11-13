package it.pa.notifica.notificaservice.strategy;

import it.pa.notifica.notificaservice.config.RivalutazioneProperties;
import it.pa.notifica.notificaservice.entity.Posizione;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RivalutazioneTari implements StrategiaRivalutazione {

    private final RivalutazioneProperties props;

    public RivalutazioneTari(RivalutazioneProperties props) {
        this.props = props;
    }

    @Override
    public String tipoTributo() {
        return "TARI";
    }

    @Override
    public BigDecimal calcolaImporto(Posizione posizione, long giorniRitardo) {
        BigDecimal originale = posizione.getImportoOriginale();

        BigDecimal sanzione = FasciaSanzione.perGiorni(giorniRitardo)
                .calcola(originale, giorniRitardo);

        BigDecimal interessi = originale
                .multiply(props.getTassoLegale())
                .multiply(BigDecimal.valueOf(giorniRitardo))
                .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);

        return originale.add(sanzione).add(interessi).setScale(2, RoundingMode.HALF_UP);
    }
}
