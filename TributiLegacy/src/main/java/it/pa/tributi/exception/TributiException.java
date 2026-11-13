package it.pa.tributi.exception;

public class TributiException extends Exception {

    private final String codice;

    public TributiException(String codice, String messaggio) {
        super(messaggio);
        this.codice = codice;
    }

    public TributiException(String codice, String messaggio, Throwable causa) {
        super(messaggio, causa);
        this.codice = codice;
    }

    public String getCodice() {
        return codice;
    }
}
