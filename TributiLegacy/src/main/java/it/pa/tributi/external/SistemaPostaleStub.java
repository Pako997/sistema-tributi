package it.pa.tributi.external;

import it.pa.tributi.dto.PosizioneDTO;

import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class SistemaPostaleStub implements ISistemaPostale {

    @Override
    @SuppressWarnings("java:S106")
    public void inviaBlocco(List<PosizioneDTO> blocco) {
        for (PosizioneDTO p : blocco) {
            System.out.println("[POSTA] Invio avviso " + p.getIuv()
                    + " a " + p.getCodiceFiscale()
                    + " - scadenza " + p.getDataScadenza());
        }
    }
}
