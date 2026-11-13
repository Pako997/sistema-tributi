package it.pa.notifica.notificaservice.strategy;

import it.pa.notifica.notificaservice.config.RivalutazioneProperties;
import it.pa.notifica.notificaservice.entity.Posizione;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RivalutazioneMulta implements StrategiaRivalutazione {

    private final RivalutazioneProperties props;

    public RivalutazioneMulta(RivalutazioneProperties props) {
        this.props = props;
    }

    @Override
    public String tipoTributo() {
        return "MULTA";
    }

    @Override
    public BigDecimal calcolaImporto(Posizione posizione, long giorniRitardo) {
        if (giorniRitardo <= props.getMultaGiorniTermine()) {
            return posizione.getImportoOriginale();
        }

        BigDecimal importo = posizione.getImportoOriginale().multiply(BigDecimal.TWO);

        long semestri = (giorniRitardo - props.getMultaGiorniTermine()) / props.getMultaGiorniSemestre();
        if (semestri > 0) {
            BigDecimal maggiorazione = importo
                    .multiply(props.getMultaMaggiorazioneSemestrale())
                    .multiply(BigDecimal.valueOf(semestri));

            BigDecimal tetto = importo.multiply(props.getMultaTettoMaggiorazione());
            importo = importo.add(maggiorazione.min(tetto));
        }

        return importo.setScale(2, RoundingMode.HALF_UP);
    }
}
