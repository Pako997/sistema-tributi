package it.pa.tributi.dao;

import it.pa.tributi.entity.Notifica;
import it.pa.tributi.exception.CodiciErrore;
import it.pa.tributi.exception.TributiException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Stateless
public class NotificaDAO implements INotificaDAO {

    @PersistenceContext(unitName = "tributiPU")
    private EntityManager em;

    @Override
    public void registraNotifica(String iuv, String tipo) throws TributiException {
        try {
            Notifica notifica = new Notifica();
            notifica.setIuv(iuv);
            notifica.setTipo(tipo);
            notifica.setDataInvio(new Date());
            em.persist(notifica);
            em.flush();
        } catch (Exception e) {
            throw new TributiException(CodiciErrore.CODICE_ERRORE_INTERNO,
                    CodiciErrore.MESSAGGIO_ERRORE_INTERNO, e);
        }
    }

    @Override
    public List<Notifica> visualNotifica(){

        String query = " SELECT p FROM Notifica p ";
        return em.createQuery(query, Notifica.class).getResultList();

    }
}
