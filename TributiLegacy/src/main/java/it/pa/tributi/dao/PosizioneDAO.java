package it.pa.tributi.dao;

import it.pa.tributi.dto.PosizioneDTO;
import it.pa.tributi.entity.Ente;
import it.pa.tributi.entity.Posizione;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class PosizioneDAO implements IPosizioneDAO{

    @PersistenceContext(unitName = "tributiPU")
    private EntityManager em;

    @Override
    public void aggiornaPosizioneInCorso(String iuv) throws TributiException {
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
            p.setStato("IN_CORSO");
            //em.merge(p);

        } catch (TributiException e) {
            throw e;
        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
    }

    @Override
    public void aggiornaPosizionePagata(String iuv, Date dataPagamento) throws TributiException {
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
            p.setStato("PAGATA");
            p.setDataPagamento(dataPagamento);
            //em.merge(p);

        } catch (TributiException e) {
            throw e;
        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
    }

    @Override
    public PosizioneDTO findPosizioneByIUV(String iuv) throws TributiException {
        PosizioneDTO pos = null;
        try{
            //l'iuv ha unique quindi posso prenderlo senza rischiare
            String query = " SELECT p FROM Posizione p WHERE p.iuv = :iuv";

            List<Posizione> poss = em.createQuery(query, Posizione.class)
                    .setParameter("iuv", iuv)
                    .getResultList();

            if (poss.isEmpty())
                throw new TributiException(CodiciErrore.CODICE_IUV_PAGAMENTO_NOT_FOUND, CodiciErrore.MESSAGGIO_IUV_PAGAMENTO_NOT_FOUND);
            pos = new PosizioneDTO();
            Posizione p = poss.get(0);
            pos.setIuv(p.getIuv());
            pos.setCodiceFiscale(p.getCodiceFiscale());
            pos.setTipoTributo(p.getTipoTributo());
            pos.setImporto(p.getImporto());
            pos.setImportoOriginale(p.getImportoOriginale());
            pos.setStato(p.getStato());
            pos.setDataEmissione(p.getDataEmissione());
            pos.setDataScadenza(p.getDataScadenza());
            pos.setDataPagamento(p.getDataPagamento());
            pos.setCodiceEnte(p.getEnte().getCodiceEnte());
            pos.setEnteEmittente(p.getEnte().getDenominazione());
            pos.setContattoEnte(p.getEnte().getContatto());
        }catch (TributiException e) {
            throw e;
        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
        return pos;
    }

    @Override
    public List<PosizioneDTO> findPosizioniByCodiceFiscale(String codiceFiscale) {
        String query = " SELECT p FROM Posizione p WHERE p.codiceFiscale = :cf ";
        List<Posizione> posizioni = em.createQuery(query, Posizione.class)
                .setParameter("cf", codiceFiscale)
                .getResultList();
        List<PosizioneDTO> posizioneDTOS = new ArrayList<>();
        for (Posizione p : posizioni) {
            PosizioneDTO pos = new PosizioneDTO();
            pos.setIuv(p.getIuv());
            pos.setCodiceFiscale(p.getCodiceFiscale());
            pos.setTipoTributo(p.getTipoTributo());
            pos.setImporto(p.getImporto());
            pos.setImportoOriginale(p.getImportoOriginale());
            pos.setStato(p.getStato());
            pos.setDataEmissione(p.getDataEmissione());
            pos.setDataScadenza(p.getDataScadenza());
            pos.setDataPagamento(p.getDataPagamento());
            pos.setCodiceEnte(p.getEnte().getCodiceEnte());
            pos.setEnteEmittente(p.getEnte().getDenominazione());
            pos.setContattoEnte(p.getEnte().getContatto());
            posizioneDTOS.add(pos);
        }
        return posizioneDTOS;
    }

    @Override
    @Transactional
    public Posizione createPagamento(String iuv,
                                   String codiceFiscale,
                                   String tipoTributo,
                                   BigDecimal importo,
                                     BigDecimal importoOriginale,
                                   String stato,
                                   Date dataEmissione,
                                   Date dataScadenza,
                                   Ente ente
                                   ){

        Posizione pos = new Posizione();
        pos.setIuv(iuv);
        pos.setCodiceFiscale(codiceFiscale);
        pos.setTipoTributo(tipoTributo);
        pos.setImporto(importo);
        pos.setImportoOriginale(importoOriginale);
        pos.setStato(stato);
        pos.setDataEmissione(dataEmissione);
        pos.setDataScadenza(dataScadenza);
        pos.setDataPagamento(null);
        pos.setEnte(ente);

        em.persist(pos);
        em.flush();

        return pos;
    }
}
