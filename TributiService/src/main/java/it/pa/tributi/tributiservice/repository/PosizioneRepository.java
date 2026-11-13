package it.pa.tributi.tributiservice.repository;

import it.pa.tributi.tributiservice.entity.Posizione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosizioneRepository  extends JpaRepository<Posizione,Long> {
    Optional<Posizione> findByIuv(String iuv);
    List<Posizione> findByCodiceFiscale(String codiceFiscale);
}
