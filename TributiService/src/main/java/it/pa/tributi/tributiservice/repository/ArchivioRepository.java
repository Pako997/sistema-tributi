package it.pa.tributi.tributiservice.repository;

import it.pa.tributi.tributiservice.entity.ArchivioPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivioRepository extends JpaRepository<ArchivioPagamento,Long> {
}
