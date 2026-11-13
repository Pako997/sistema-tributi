package it.pa.tributi.tributiservice.external;


import it.pa.tributi.tributiservice.dto.PosizioneDTO;

import java.math.BigDecimal;

public interface SistemaPagamento {
    String autorizza(PosizioneDTO posizione, BigDecimal importo);
}
