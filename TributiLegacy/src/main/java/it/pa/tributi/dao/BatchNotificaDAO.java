package it.pa.tributi.dao;

import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.entity.Posizione;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Stateless
public class BatchNotificaDAO implements IBatchNotificaDAO {

    @PersistenceContext(unitName = "tributiPU")
    private EntityManager em;

    @Override
    public List<PosizioneDTO> findPosizioniDaNotificare(int giorniAllaScadenza, String tipoNotifica)
            throws TributiException {
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, giorniAllaScadenza);
            Date limite = cal.getTime();

            String query = " SELECT p FROM Posizione p "
                    + " WHERE p.stato = 'DA_PAGARE' "
                    + " AND p.dataScadenza <= :limite "
                    + " AND NOT EXISTS ( SELECT n FROM Notifica n "
                    + "                  WHERE n.iuv = p.iuv AND n.tipo = :tipo ) ";

            List<Posizione> posizioni = em.createQuery(query, Posizione.class)
                    .setParameter("limite", limite)
                    .setParameter("tipo", tipoNotifica)
                    .getResultList();

            List<PosizioneDTO> result = new ArrayList<PosizioneDTO>();
            for (Posizione p : posizioni) {
                PosizioneDTO dto = new PosizioneDTO();
                dto.setIuv(p.getIuv());
                dto.setCodiceFiscale(p.getCodiceFiscale());
                dto.setTipoTributo(p.getTipoTributo());
                dto.setImporto(p.getImporto());
                dto.setStato(p.getStato());
                dto.setDataEmissione(p.getDataEmissione());
                dto.setDataScadenza(p.getDataScadenza());
                dto.setCodiceEnte(p.getEnte().getCodiceEnte());
                dto.setEnteEmittente(p.getEnte().getDenominazione());
                dto.setContattoEnte(p.getEnte().getContatto());
                result.add(dto);
            }
            return result;

        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
    }

    @Override
    public List<PosizioneDTO> findPosizioniDaNotificare(int giorniMin, int giorniMax, String tipoNotifica)
            throws TributiException {
        try {
            Calendar calMin = Calendar.getInstance();
            calMin.add(Calendar.DAY_OF_MONTH, giorniMin);
            Date limiteMin = calMin.getTime();

            Calendar calMax = Calendar.getInstance();
            calMax.add(Calendar.DAY_OF_MONTH, giorniMax);
            Date limiteMax = calMax.getTime();

            String query = " SELECT p FROM Posizione p "
                    + " WHERE p.stato = 'DA_PAGARE' "
                    + " AND p.dataScadenza > :limiteMin "
                    + " AND p.dataScadenza <= :limiteMax "
                    + " AND NOT EXISTS ( SELECT n FROM Notifica n "
                    + "                  WHERE n.iuv = p.iuv AND n.tipo = :tipo ) ";

            List<Posizione> posizioni = em.createQuery(query, Posizione.class)
                    .setParameter("limiteMin", limiteMin)
                    .setParameter("limiteMax", limiteMax)
                    .setParameter("tipo", tipoNotifica)
                    .getResultList();

            List<PosizioneDTO> result = new ArrayList<PosizioneDTO>();
            for (Posizione p : posizioni) {
                PosizioneDTO dto = new PosizioneDTO();
                dto.setIuv(p.getIuv());
                dto.setCodiceFiscale(p.getCodiceFiscale());
                dto.setTipoTributo(p.getTipoTributo());
                dto.setImporto(p.getImporto());
                dto.setStato(p.getStato());
                dto.setDataEmissione(p.getDataEmissione());
                dto.setDataScadenza(p.getDataScadenza());
                dto.setCodiceEnte(p.getEnte().getCodiceEnte());
                dto.setEnteEmittente(p.getEnte().getDenominazione());
                dto.setContattoEnte(p.getEnte().getContatto());
                result.add(dto);
            }
            return result;

        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
    }

    @Override
    public List<PosizioneDTO> findPosizioniScadute() throws TributiException {
        try {
            String query = " SELECT p FROM Posizione p "
                    + " WHERE p.stato = 'DA_PAGARE' "
                    + " AND p.dataScadenza < :oggi "
                    + " AND p.tipoTributo IN ('MULTA', 'TARI') ";

            List<Posizione> posizioni = em.createQuery(query, Posizione.class)
                    .setParameter("oggi", new Date())
                    .getResultList();

            List<PosizioneDTO> result = new ArrayList<PosizioneDTO>();
            for (Posizione p : posizioni) {
                PosizioneDTO dto = new PosizioneDTO();
                dto.setIuv(p.getIuv());
                dto.setCodiceFiscale(p.getCodiceFiscale());
                dto.setTipoTributo(p.getTipoTributo());
                dto.setImporto(p.getImporto());
                dto.setImportoOriginale(p.getImportoOriginale());
                dto.setStato(p.getStato());
                dto.setDataEmissione(p.getDataEmissione());
                dto.setDataScadenza(p.getDataScadenza());
                dto.setDataPagamento(p.getDataPagamento());
                dto.setCodiceEnte(p.getEnte().getCodiceEnte());
                dto.setEnteEmittente(p.getEnte().getDenominazione());
                dto.setContattoEnte(p.getEnte().getContatto());
                result.add(dto);
            }
            return result;

        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
    }

    @Override
    public void aggiornaImporto(String iuv, BigDecimal nuovoImporto) throws TributiException {
        try {
            String query = " SELECT p FROM Posizione p WHERE p.iuv = :iuv ";

            List<Posizione> poss = em.createQuery(query, Posizione.class)
                    .setParameter("iuv", iuv)
                    .getResultList();

            if (poss.isEmpty()) {
                throw new TributiException(CodiciErrore.CODICE_IUV_PAGAMENTO_NOT_FOUND,
                        CodiciErrore.MESSAGGIO_IUV_PAGAMENTO_NOT_FOUND);
            }

            Posizione p = poss.get(0);
            p.setImporto(nuovoImporto);
            //em.merge(p);

        } catch (TributiException e) {
            throw e;
        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
    }

    @Override
    public List<PosizioneDTO> findPosizioniDaNotificareEmissione() throws TributiException {
        try {
            String query = " SELECT p FROM Posizione p "
                    + " WHERE p.stato = 'DA_PAGARE' "
                    + " AND NOT EXISTS ( SELECT n FROM Notifica n "
                    + "                  WHERE n.iuv = p.iuv AND n.tipo = 'EMISSIONE' ) ";

            List<Posizione> posizioni = em.createQuery(query, Posizione.class)
                    .getResultList();

            List<PosizioneDTO> result = new ArrayList<PosizioneDTO>();
            for (Posizione p : posizioni) {
                PosizioneDTO dto = new PosizioneDTO();
                dto.setIuv(p.getIuv());
                dto.setCodiceFiscale(p.getCodiceFiscale());
                dto.setTipoTributo(p.getTipoTributo());
                dto.setImporto(p.getImporto());
                dto.setStato(p.getStato());
                dto.setDataEmissione(p.getDataEmissione());
                dto.setDataScadenza(p.getDataScadenza());
                dto.setCodiceEnte(p.getEnte().getCodiceEnte());
                dto.setEnteEmittente(p.getEnte().getDenominazione());
                dto.setContattoEnte(p.getEnte().getContatto());
                result.add(dto);
            }
            return result;

        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
    }
}
