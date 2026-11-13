package it.pa.tributi.tributiservice.exception;

import lombok.Getter;

@Getter
public enum ErroreTributi {

    AUTENTICAZIONE_MANCANTE("AUTH_000", "Informazioni di autenticazione mancanti"),
    UTENTE_NON_AUTORIZZATO("AUTH_001", "Utente non autorizzato"),
    ERRORE_INTERNO("INT_001", "Errore interno"),
    FORBIDDEN("FORB_001", "L'operazione richiesta non e' consentita"),
    ENTE_NON_TROVATO("NFOU_001", "Ente non trovato"),
    PARAMETRI_MANCANTI("PMISS_001", "Uno o piu parametri forniti non sono stati valorizzati"),
    IMPORTO_NULLO("IMPO_001", "Importo nullo, impossibile procedere"),
    DATA_SFORMATA("DATA_001", "La data fornita in input non segue il formato yyyy-MM-dd"),
    DATA_NON_PARSABILE("DATA_002", "La data fornita in input non e' valida"),
    SCADENZA_NON_VALIDA("DATA_003", "La data scadenza deve essere almeno a 30 giorni dalla data emissione"),
    IUV_NON_TROVATO("NIUV_001", "Identificativo versamento non trovato"),
    PAGAMENTO_NON_RIUSCITO("PERR_001", "Pagamento non riuscito. Verificare il metodo");

    private final String codice;
    private final String messaggio;

    ErroreTributi(String codice, String messaggio) {
        this.codice = codice;
        this.messaggio = messaggio;
    }
}