package it.pa.notifica.notificaservice.repository;

import it.pa.notifica.notificaservice.entity.Posizione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PosizioneRepository extends JpaRepository<Posizione, Long> {

    @Query("SELECT p FROM Posizione p " +
            "WHERE p.stato = 'DA_PAGARE' " +
            "AND p.dataScadenza > :limiteMin " +
            "AND p.dataScadenza <= :limiteMax " +
            "AND NOT EXISTS (SELECT n FROM Notifica n WHERE n.iuv = p.iuv AND n.tipo = :tipo)")
    List<Posizione> findDaNotificare(@Param("limiteMin") LocalDate limiteMin,
                                     @Param("limiteMax") LocalDate limiteMax,
                                     @Param("tipo") String tipo);

    @Query("SELECT p FROM Posizione p " +
            "WHERE p.stato = 'DA_PAGARE' AND p.dataScadenza < :oggi")
    List<Posizione> findScadute(@Param("oggi") LocalDate oggi);
}
