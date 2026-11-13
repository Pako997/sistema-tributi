package it.pa.tributi.batch;

import it.pa.tributi.dao.IBatchNotificaDAO;
import it.pa.tributi.dao.INotificaDAO;
import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.exception.TributiException;
import it.pa.tributi.external.ISistemaPostale;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Singleton
public class NotificheBatch {

    @EJB
    private IBatchNotificaDAO batchDAO;

    @EJB
    private INotificaDAO notificaDAO;

    @EJB
    private ISistemaPostale sistemaPostale;

    @Schedule(hour = "*", minute = "*", persistent = false)
    public void eseguiNotifiche() {
        try {
            aggiornaImporti();
            List<PosizioneDTO> bloccoEmissione = batchDAO.findPosizioniDaNotificareEmissione();
            if (!bloccoEmissione.isEmpty()) {
                sistemaPostale.inviaBlocco(bloccoEmissione);
                for (PosizioneDTO p : bloccoEmissione) {
                    notificaDAO.registraNotifica(p.getIuv(), "EMISSIONE");
                }
            }

            List<PosizioneDTO> blocco15 = batchDAO.findPosizioniDaNotificare(0, 15, "SOLLECITO15");
            if (!blocco15.isEmpty()) {
                sistemaPostale.inviaBlocco(blocco15);
                for (PosizioneDTO p : blocco15) {
                    notificaDAO.registraNotifica(p.getIuv(), "SOLLECITO15");
                }
            }

            List<PosizioneDTO> blocco30 = batchDAO.findPosizioniDaNotificare(15, 30, "SOLLECITO30");
            if (!blocco30.isEmpty()) {
                sistemaPostale.inviaBlocco(blocco30);
                for (PosizioneDTO p : blocco30) {
                    notificaDAO.registraNotifica(p.getIuv(), "SOLLECITO30");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void aggiornaImporti() throws TributiException {
        List<PosizioneDTO> scadute = batchDAO.findPosizioniScadute();
        for (PosizioneDTO p : scadute) {
            if ("MULTA".equals(p.getTipoTributo())) {
                aggiornaMulta(p);
            } else if ("TARI".equals(p.getTipoTributo())) {
                aggiornaTari(p);
            }
        }
    }

    private void aggiornaTari(PosizioneDTO p) throws TributiException {
        long giorniRitardo = (new Date().getTime() - p.getDataScadenza().getTime())
                / (1000 * 60 * 60 * 24);

        if (giorniRitardo <= 0) {
            return;
        }

        BigDecimal originale = p.getImportoOriginale();
        BigDecimal sanzione;

        if (giorniRitardo <= 14) {
            sanzione = originale.multiply(new BigDecimal("0.000833"))
                    .multiply(new BigDecimal(giorniRitardo));
        } else if (giorniRitardo <= 30) {
            sanzione = originale.multiply(new BigDecimal("0.0125"));
        } else if (giorniRitardo <= 90) {
            sanzione = originale.multiply(new BigDecimal("0.0139"));
        } else {
            sanzione = originale.multiply(new BigDecimal("0.03125"));
        }

        BigDecimal interessi = originale
                .multiply(new BigDecimal("1.60"))
                .multiply(new BigDecimal(giorniRitardo))
                .divide(new BigDecimal("36500"), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal nuovoImporto = originale.add(sanzione).add(interessi)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        if (nuovoImporto.compareTo(p.getImporto()) != 0) {
            batchDAO.aggiornaImporto(p.getIuv(), nuovoImporto);
        }
    }

    private void aggiornaMulta(PosizioneDTO p) throws TributiException {
        long giorniRitardo = (new Date().getTime() - p.getDataScadenza().getTime())
                / (1000 * 60 * 60 * 24);

        if (giorniRitardo <= 60) {
            return;
        }

        BigDecimal nuovoImporto = p.getImportoOriginale().multiply(new BigDecimal("2"));

        long semestri = (giorniRitardo - 60) / 180;
        if (semestri > 0) {
            BigDecimal maggiorazione = nuovoImporto
                    .multiply(new BigDecimal("0.10"))
                    .multiply(new BigDecimal(semestri));

            BigDecimal tetto = nuovoImporto.multiply(new BigDecimal("0.60"));
            if (maggiorazione.compareTo(tetto) > 0) {
                maggiorazione = tetto;
            }
            nuovoImporto = nuovoImporto.add(maggiorazione);
        }

        nuovoImporto = nuovoImporto.setScale(2, BigDecimal.ROUND_HALF_UP);

        if (nuovoImporto.compareTo(p.getImporto()) != 0) {
            batchDAO.aggiornaImporto(p.getIuv(), nuovoImporto);
        }
    }
}