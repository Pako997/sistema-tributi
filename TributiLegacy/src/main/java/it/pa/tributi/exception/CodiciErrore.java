package it.pa.tributi.exception;

public class CodiciErrore {
    public static final String CODICE_AUTENTICAZIONE_MANCANTE = "AUTH_000";
    public static final String CODICE_UTENTE_NON_AUTORIZZATO = "AUTH_001";
    public static final String CODICE_ERRORE_INTERNO = "INT_001";
    public static final String CODICE_FORBIDDEM = "FORB_001";
    public static final String CODICE_NOTFOUND = "NFOU_001";
    public static final String CODICE_PARAMETRI_MANCANTI = "PMISS_001";
    public static final String CODICE_IMPORTO_NULLO = "IMPO_001";
    public static final String CODICE_DATA_SFORMATA = "DATA_001";
    public static final String CODICE_DATA_NON_PARSABILE = "DATA_002";
    public static final String CODICE_SCADENZA_NON_VALIDA = "DATA_003";
    public static final String CODICE_IUV_PAGAMENTO_NOT_FOUND = "NIUV_001";
    public static final String CODICE_PAGAMENTO_NON_RIUSCITO = "PERR_001";




    public static final String MESSAGGIO_AUTENTICAZIONE_MANCANTE = "Informazioni di autenticazione mancanti";
    public static final String MESSAGGIO_UTENTE_NON_AUTORIZZATO = "Utente non autorizzato";
    public static final String MESSAGGIO_ERRORE_INTERNO = "Errore interno";
    public static final String MESSAGGIO_FORBIDDEM = "L'operazione richiesta non è consentita";
    public static final String MESSAGGIO_ENTE_NOTFOUND = "Ente non trovato";
    public static final String MESSAGGIO_PARAMETRI_MANCANTI = "Uno o piu parametri forniti non sono stati valorizzati";
    public static final String MESSAGGIO_IMPORTO_NULLO = "Importo nullo, impossibile procedere";
    public static final String MESSAGGIO_DATA_SFORMATA = "La data fornita in input non segue il formato yyyy-MM-dd";
    public static final String MESSAGGIO_DATA_NON_PARSABILE = "La data fornita in input non è valida";
    public static final String MESSAGGIO_SCADENZA_NON_VALIDA = "La data scadenza deve essere almeno a 30 giorni dalla data emissione";
    public static final String MESSAGGIO_IUV_PAGAMENTO_NOT_FOUND = "Identificativo versamento non trovato";
    public static final String MESSAGGIO_PAGAMENTO_NON_RIUSCITO = "Pagamento non riuscito. Verificare il metodo";



/* questa diventerà
* public enum ErroreTributi {

    AUTENTICAZIONE_MANCANTE("AUTH_001", "Informazioni di autenticazione mancanti"),
    ACCESSO_NEGATO("AUTH_002", "Accesso negato"),
    ENTE_NON_TROVATO("NF_001", "Ente non trovato"),
    ERRORE_INTERNO("INT_001", "Errore interno");

    private final String codice;
    private final String messaggio;

    ErroreTributi(String codice, String messaggio) {
        this.codice = codice;
        this.messaggio = messaggio;
    }

    public String getCodice() { return codice; }
    public String getMessaggio() { return messaggio; }
}
*
* e public TributiException(ErroreTributi errore) {
    super(errore.getMessaggio());
    this.errore = errore;
} "Per Fowler, il refactoring si chiama Replace Type Code with Class (cap. 12 nella seconda edizione)
* o più semplicemente è un caso di Primitive Obsession risolto."
* */

}
