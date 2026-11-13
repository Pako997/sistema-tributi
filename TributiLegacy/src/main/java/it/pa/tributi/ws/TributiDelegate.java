package it.pa.tributi.ws;

import it.pa.tributi.context.ServiceLocator;
import it.pa.tributi.context.UtenteContext;
import it.pa.tributi.dao.INotificaDAO;
import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.dto.RicevutaDTO;
import it.pa.tributi.entity.Notifica;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;
import it.pa.tributi.service.IOperatoreService;
import it.pa.tributi.service.IPagamentoService;
import it.pa.tributi.service.IPosizioneService;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.management.ServiceNotFoundException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@WebService(endpointInterface = "it.pa.tributi.ws.TributiService")
@HandlerChain(file = "handler-chain.xml")
public class TributiDelegate implements TributiService{

    @Override
    public List<Notifica> visualNotifiche() throws ServiceNotFoundException {
        INotificaDAO noDao = ServiceLocator.get(INotificaDAO.class);
        return noDao.visualNotifica();
    }

    @Override
    public RicevutaDTO eseguiPagamento(String iuv, Double importoVersato) throws TributiException {
        try{
            IPosizioneService poServ = ServiceLocator.get(IPosizioneService.class);
            //il metodo sotto serve a segnare come IN_CORSO le posizioni in via di pagamento
            //se la transazione in eseguiPagamento fallisce ad un punto ma il pagamento è andato
            //nel servizio di pagamento esterno, posso recuperare le posizioni rimaste in pending
            poServ.aggiornaPosizioneInCorso(iuv);
            IPagamentoService service =  ServiceLocator.get(IPagamentoService.class);

            return service.eseguiPagamento(iuv, importoVersato);
        } catch (TributiException e) {
            throw e;
        }catch (Exception e){
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,CodiciErrore.MESSAGGIO_ERRORE_INTERNO + " : " + e.getMessage(), e );
        }
    }

    public List<PosizioneDTO> verificaPosizione(String codiceFiscale) throws TributiException {
        try{
            if (codiceFiscale == null || codiceFiscale.trim().isEmpty()){
                throw new TributiException(CodiciErrore.CODICE_PARAMETRI_MANCANTI,
                        CodiciErrore.MESSAGGIO_PARAMETRI_MANCANTI);
            }

            String cfAutenticato = UtenteContext.getCodiceFiscale();
            if (cfAutenticato == null || cfAutenticato.trim().isEmpty()) {
                throw new TributiException(CodiciErrore.CODICE_AUTENTICAZIONE_MANCANTE,
                        CodiciErrore.MESSAGGIO_AUTENTICAZIONE_MANCANTE);
            }

            IOperatoreService operatoreServ = ServiceLocator.get(IOperatoreService.class);
            String ruolo = operatoreServ.getRuolo(cfAutenticato);

            IPosizioneService service = ServiceLocator.get(IPosizioneService.class);
            List<PosizioneDTO> result = null;
            if("OPERATORE".equals(ruolo)){
                result = service.verificaPosizione(codiceFiscale);
            }else{
                result = service.verificaPosizione(cfAutenticato);
            }

            return result;
        } catch (TributiException e) {
            throw e;
        }catch (Exception e){
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,CodiciErrore.MESSAGGIO_ERRORE_INTERNO + " : " + e.getMessage(), e );
        }
    }




    //operazione che permette di registrare il pagamento
    //solo op per operatori
    //prende in input n informazioni
    //si refattorizza raggruppando tutte le info i nun solo oggetto -> "Introduce Parameter Object"
    //genero l'iuv nel metodo del service, mischiato con la logica, poi nel refactor faccio vedere che
    //estraggo il metodo di generazione dell'iuv in un metodo a parte -> "Extract Method"
    public String registraPagamento(//iuv da generare
                                    String codiceFiscale,
                                    String tipoTributo,
                                    BigDecimal importo,
                                    //stato DA_PAGARE
                                    //String dataEmissione data oggi +1
                                    String dataScadenza,
                                    String codiceEnte
                                    ) throws TributiException{
        if (codiceFiscale == null || codiceFiscale.trim().isEmpty()
                || tipoTributo == null || tipoTributo.trim().isEmpty()
                || importo == null
                || dataScadenza == null || dataScadenza.trim().isEmpty()
                || codiceEnte == null || codiceEnte.trim().isEmpty()) {
            throw new TributiException(CodiciErrore.CODICE_PARAMETRI_MANCANTI,
                    CodiciErrore.MESSAGGIO_PARAMETRI_MANCANTI);
        }
        if ( importo.compareTo(BigDecimal.ZERO) <= 0){
            throw new TributiException(CodiciErrore.CODICE_IMPORTO_NULLO,
                    CodiciErrore.MESSAGGIO_IMPORTO_NULLO);
        }
        if (!dataScadenza.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new TributiException(CodiciErrore.CODICE_DATA_SFORMATA,CodiciErrore.MESSAGGIO_DATA_SFORMATA);
        }

        String resultIuv = null;
        try{
           String cfAutenticato = UtenteContext.getCodiceFiscale();
           if (cfAutenticato == null || cfAutenticato.trim().isEmpty()) {
               throw new TributiException(CodiciErrore.CODICE_AUTENTICAZIONE_MANCANTE,
                       CodiciErrore.MESSAGGIO_AUTENTICAZIONE_MANCANTE);}
           IOperatoreService operatoreServ = ServiceLocator.get(IOperatoreService.class);
           String ruolo = operatoreServ.getRuolo(cfAutenticato);

            IPosizioneService service = ServiceLocator.get(IPosizioneService.class);

            /*Si assume che un avviso di pagamento debba essere emesso con un anticipo minimo di trenta giorni rispetto
            alla scadenza, in modo da garantire al contribuente il tempo necessario alla ricezione della comunicazione
            e all'adempimento. Tale vincolo è verificato in fase di creazione dell'avviso e costituisce il presupposto
            su cui si fonda la pianificazione delle notifiche: il sistema può assumere l'esistenza di una finestra
            temporale sufficiente a inviare la comunicazione di emissione e gli eventuali solleciti.*/

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date scadenza = sdf.parse(dataScadenza);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 30);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            if (scadenza.before(cal.getTime())) {
                throw new TributiException(CodiciErrore.CODICE_SCADENZA_NON_VALIDA,
                        CodiciErrore.MESSAGGIO_SCADENZA_NON_VALIDA);
            }

            if("OPERATORE".equals(ruolo)){
               resultIuv = service.createPagamento(codiceFiscale,tipoTributo,importo,dataScadenza,codiceEnte);
            }else{
               throw new TributiException(CodiciErrore.CODICE_FORBIDDEM,
                       CodiciErrore.MESSAGGIO_FORBIDDEM);
            }

        }catch (ParseException e) {
            throw new TributiException(CodiciErrore.CODICE_DATA_NON_PARSABILE,
                    CodiciErrore.MESSAGGIO_DATA_NON_PARSABILE, e);
        }catch (TributiException e) {
            throw e;
        }catch (Exception e){
           throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,CodiciErrore.MESSAGGIO_ERRORE_INTERNO + " : " + e.getMessage(), e );
        }
        return resultIuv;
    }



}
