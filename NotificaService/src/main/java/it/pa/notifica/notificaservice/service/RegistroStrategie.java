package it.pa.notifica.notificaservice.service;

import it.pa.notifica.notificaservice.strategy.StrategiaRivalutazione;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RegistroStrategie {

    private final Map<String, StrategiaRivalutazione> strategie;

    public RegistroStrategie(List<StrategiaRivalutazione> disponibili) {
        this.strategie = disponibili.stream()
                .collect(Collectors.toMap(StrategiaRivalutazione::tipoTributo,
                        Function.identity()));
    }

    public Optional<StrategiaRivalutazione> per(String tipoTributo) {
        return Optional.ofNullable(strategie.get(tipoTributo));
    }
}
