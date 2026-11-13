package it.pa.tributi.external;


import it.pa.tributi.dto.PosizioneDTO;

import javax.ejb.Local;
import java.util.List;

@Local
public interface ISistemaPostale {
    void inviaBlocco(List<PosizioneDTO> blocco);
}
