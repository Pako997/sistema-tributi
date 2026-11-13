package it.pa.tributi.tributiservice.external;

import it.pa.tributi.tributiservice.dto.PosizioneDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class SistemaPagamentoStub implements SistemaPagamento {

    private static final Logger log = LoggerFactory.getLogger(SistemaPagamentoStub.class);

    @Override
    public String autorizza(PosizioneDTO posizione, BigDecimal importo) {
        log.info("Pagamento {} - importo versato {}", posizione.getIuv(), importo);
        return UUID.randomUUID().toString().replace("-", "");
    }
}
