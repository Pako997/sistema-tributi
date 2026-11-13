package it.pa.tributi.external;

import it.pa.tributi.dto.PosizioneDTO;

import javax.ejb.Stateless;
import java.util.UUID;

@Stateless
public class SistemaPagamentoStub implements ISistemaPagamento{


    @Override
    @SuppressWarnings("java:S106")
    public String verificaPagamento(PosizioneDTO p, Double importo) {
        System.out.println("[PAGAMENTO] Pagamento di " + p.getIuv()
                + " versati: " + importo);
        return UUID.randomUUID().toString().replace("-", "");
    }
}
