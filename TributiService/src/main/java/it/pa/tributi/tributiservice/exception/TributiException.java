package it.pa.tributi.tributiservice.exception;

import lombok.Getter;

@Getter
public class TributiException extends RuntimeException {

    private final ErroreTributi errore;

    public TributiException(ErroreTributi errore) {
        super(errore.getMessaggio());
        this.errore = errore;
    }

    public TributiException(ErroreTributi errore, Throwable causa) {
        super(errore.getMessaggio(), causa);
        this.errore = errore;
    }
}
