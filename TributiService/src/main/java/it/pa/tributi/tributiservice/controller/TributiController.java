package it.pa.tributi.tributiservice.controller;

import it.pa.tributi.tributiservice.dto.DebitoRequest;
import it.pa.tributi.tributiservice.dto.PagamentoRequest;
import it.pa.tributi.tributiservice.dto.PosizioneDTO;
import it.pa.tributi.tributiservice.dto.RicevutaDTO;
import it.pa.tributi.tributiservice.service.PagamentoService;
import it.pa.tributi.tributiservice.service.PosizioneService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TributiController {
    private final PagamentoService pagamentoService;
    private final PosizioneService posizioneService;

    public TributiController(PosizioneService posizioneService,
                             PagamentoService pagamentoService) {
        this.posizioneService = posizioneService;
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/pagamenti")
    public RicevutaDTO eseguiPagamento(@RequestBody PagamentoRequest richiesta) {
        posizioneService.segnaInCorso(richiesta.iuv());
        return pagamentoService.eseguiPagamento(richiesta.iuv(), richiesta.importoVersato());
    }

    @GetMapping("/posizioni")
    public List<PosizioneDTO> verificaPosizione(
            @RequestParam(required = false) String codiceFiscale,
            Authentication autenticazione) {

        String cfAutenticato = autenticazione.getName();

        boolean operatore = autenticazione.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OPERATORE"));

        return posizioneService.verificaPosizione(operatore ? codiceFiscale : cfAutenticato);
    }

    @PostMapping("/posizioni")
    @PreAuthorize("hasRole('OPERATORE')")
    public Map<String, String> registraDebito(@Valid @RequestBody DebitoRequest richiesta) {
        return Map.of("iuv", posizioneService.creaPosizione(richiesta));
    }

}
