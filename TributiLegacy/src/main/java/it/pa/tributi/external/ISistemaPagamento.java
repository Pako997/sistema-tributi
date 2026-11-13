package it.pa.tributi.external;

import it.pa.tributi.dto.PosizioneDTO;

public interface ISistemaPagamento {

    public String verificaPagamento(PosizioneDTO pos, Double importo);
}
