package it.pa.notifica.notificaservice.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum FasciaSanzione {

    SPRINT(14, new BigDecimal("0.000833"), true),
    BREVE(30, new BigDecimal("0.0125"), false),
    INTERMEDIA(90, new BigDecimal("0.0139"), false),
    LUNGA(Long.MAX_VALUE, new BigDecimal("0.03125"), false);

    private final long giorniMax;
    private final BigDecimal aliquota;
    private final boolean proRataDie;

    FasciaSanzione(long giorniMax, BigDecimal aliquota, boolean proRataDie) {
        this.giorniMax = giorniMax;
        this.aliquota = aliquota;
        this.proRataDie = proRataDie;
    }

    public static FasciaSanzione perGiorni(long giorniRitardo) {
        for (FasciaSanzione f : values()) {
            if (giorniRitardo <= f.giorniMax) {
                return f;
            }
        }
        return LUNGA;
    }

    public BigDecimal calcola(BigDecimal importo, long giorniRitardo) {
        BigDecimal sanzione = importo.multiply(aliquota);
        if (proRataDie) {
            sanzione = sanzione.multiply(BigDecimal.valueOf(giorniRitardo));
        }
        return sanzione.setScale(2, RoundingMode.HALF_UP);
    }
}
