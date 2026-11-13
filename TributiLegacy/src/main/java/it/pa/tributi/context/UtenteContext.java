package it.pa.tributi.context;

public class UtenteContext {
    private static final ThreadLocal<String> CODICE_FISCALE = new ThreadLocal<String>();

    public static void set(String cf) {
        CODICE_FISCALE.set(cf);
    }

    public static String getCodiceFiscale() {
        return CODICE_FISCALE.get();
    }

    public static void clear() {
        CODICE_FISCALE.remove();
    }
}
